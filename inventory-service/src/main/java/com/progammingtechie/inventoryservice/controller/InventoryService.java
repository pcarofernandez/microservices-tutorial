package com.progammingtechie.inventoryservice.controller;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.progammingtechie.inventoryservice.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

	final InventoryRepository inventoryRepository;

	@Transactional(readOnly = true)
	public boolean isInStock(String skuCode) {
		return inventoryRepository.findBySkuCode(skuCode).isPresent();
	}
}
