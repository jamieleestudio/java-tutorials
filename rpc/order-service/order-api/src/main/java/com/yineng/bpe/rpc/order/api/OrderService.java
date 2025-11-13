package com.yineng.bpe.rpc.order.api;

import java.util.List;

public interface OrderService {

    List<String>  findOrderList();

    String findOrder(String orderId);

}
