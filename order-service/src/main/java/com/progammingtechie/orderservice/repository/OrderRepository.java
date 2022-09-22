package com.progammingtechie.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.progammingtechie.orderservice.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
