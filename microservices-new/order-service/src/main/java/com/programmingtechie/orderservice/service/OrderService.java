package com.programmingtechie.orderservice.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.programmingtechie.orderservice.dto.InventoryResponse;
import com.programmingtechie.orderservice.dto.OrderLineItemsDto;
import com.programmingtechie.orderservice.dto.OrderRequest;
import com.programmingtechie.orderservice.event.OrderPlacedEvent;
import com.programmingtechie.orderservice.model.Order;
import com.programmingtechie.orderservice.model.OrderLineItems;
import com.programmingtechie.orderservice.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

	private final OrderRepository orderRepository;

	private final WebClient.Builder webClientBuilder;

	private final Tracer tracer;

	private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

	public String placeOrder(OrderRequest orderRequest) {
		Order order = new Order();
		order.setOrderNumber(UUID.randomUUID().toString());

		List<OrderLineItems> orderLineItemsList = orderRequest.getOrderLineItemsDtoList().stream().map(this::mapToOrder)
				.toList();
		order.setOrderLineItemsList(orderLineItemsList);

		List<String> skuCodes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();

		log.info("Calling inventory service");

		Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");

		try (Tracer.SpanInScope spanInScope = tracer.withSpan(inventoryServiceLookup.start())) {

			// Call Inventory Service and place order if product is in stock
			InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
					.uri("http://inventory-service/api/inventory",
							uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
					.retrieve().bodyToMono(InventoryResponse[].class).block();

			boolean allProductsInStock = Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);

			if (allProductsInStock) {
				orderRepository.save(order);
				kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
				return "Order placed successfully";
			} else {
				throw new IllegalArgumentException("Product is not in stock, please try again later");
			}
		} finally {
			inventoryServiceLookup.end();
		}
	}

	private OrderLineItems mapToOrder(OrderLineItemsDto orderLineItemsDto) {
		OrderLineItems orderLineItems = new OrderLineItems();
		orderLineItems.setId(orderLineItemsDto.getId());
		orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
		orderLineItems.setPrice(orderLineItemsDto.getPrice());
		orderLineItems.setQuantity(orderLineItemsDto.getQuantity());

		return orderLineItems;
	}
}
