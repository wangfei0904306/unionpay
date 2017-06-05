package com.example.demo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface UnionPaymentService {


    /**
     * 支付
     * @param request
     * @param response
     */
    void pay(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * 后台回调
     * @param request
     * @param response
     */
    void backRcvResponse(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * 前台回调
     * @param request
     * @param response
     * @throws IOException
     */
    void frontRcvResponse(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

    /**
     * 交易状态查询
     * @param request
     * @param response
     */
    void query(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * 支付成功后的跳转
     * @param request
     * @param response
     */
    void successRedict(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * 检查支付结果
     * @param shopOrderId
     * @throws IOException
     */
    void check(Long shopOrderId);
}