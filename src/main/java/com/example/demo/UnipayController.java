package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by wangfei on 2017/4/22.
 */

@Validated
@RestController
@RequestMapping("/union")
public class UnipayController {

    @Autowired
    private UnionPaymentService service;

    /**
     * 支付
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/pay", method = {RequestMethod.POST, RequestMethod.GET})
    public void pay(HttpServletRequest request, HttpServletResponse response) throws IOException{

        service.pay(request, response);

    }

    /**
     * 后台回调
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/backRcvResponse", method = {RequestMethod.GET, RequestMethod.POST})
    public void backRcvResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {

        service.backRcvResponse(request, response);

    }

    /**
     * 前台回调
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/frontRcvResponse", method = RequestMethod.POST)
    public void frontRcvResponse(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        service.frontRcvResponse(request, response);

    }

    /**
     * 成功后跳转
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/successRedict", method = RequestMethod.POST)
    public void successRedict(HttpServletRequest request, HttpServletResponse response) throws IOException {

        service.successRedict(request, response);

    }

    /**
     * 查询、检查交易状态
     * @param request
     * @param response
     */
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public void query(HttpServletRequest request, HttpServletResponse response) throws IOException {

        service.query(request, response);

    }

    /**
     * 交易状态查询
     * @param orderId
     */
    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public JsonResult check(Long orderId) {

        try {
            service.check(orderId);
            return JsonResult.resultSuccess("已支付", "");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.resultError("未发现支付信息");
        }

    }


}
