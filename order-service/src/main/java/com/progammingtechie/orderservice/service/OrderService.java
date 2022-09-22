package com.progammingtechie.orderservice.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.progammingtechie.orderservice.dto.OrderLineItemsDto;
import com.progammingtechie.orderservice.dto.OrderRequest;
import com.progammingtechie.orderservice.model.Order;
import com.progammingtechie.orderservice.model.OrderLineItems;
import com.progammingtechie.orderservice.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

	private final OrderRepository orderRepository;

	public void placeOrder(OrderRequest orderRequest) {
		Order order = new Order();
		order.setOrderNumber(UUID.randomUUID().toString());

		List<OrderLineItems> orderLineItemsList = orderRequest.getOrderLineItemsDtoList().stream().map(this::mapToOrder)
				.toList();
		order.setOrderLineItemsList(orderLineItemsList);

		orderRepository.save(order);
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
