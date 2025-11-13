package com.yineng.bpe.rpc.order.provider;


import com.yineng.bpe.rpc.order.api.OrderService;
import com.yineng.bpe.rpc.order.provider.annotation.RemoteService;

import java.util.ArrayList;
import java.util.List;

@RemoteService
public class OrderServiceImpl implements OrderService {

    @Override
    public List<String> findOrderList() {

        List<String> orderList = new ArrayList<>();
        orderList.add("订单1");
        orderList.add("订单2");

        return orderList;
    }

    @Override
    public String findOrder(String orderId) {
        return "订单"+orderId;
    }


}
