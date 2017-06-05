package com.example.demo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UnipayController Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>���� 5, 2017</pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)  //SpringJUnit支持，由此引入Spring-Test框架支持！
@SpringBootTest(classes = UnipayApplication.class)  //指定我们SpringBoot工程的Application启动类
@WebAppConfiguration  //由于是Web项目，Junit需要模拟ServletContext，因此我们需要给我们的测试类加上@WebAppConfiguration
public class UnipayControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private UnipayController unipayController;

    @Before
    public void before() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(unipayController).build();
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: pay(HttpServletRequest request, HttpServletResponse response)
     */
    @Test
    public void testPay() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/union/pay");
        request.contentType(MediaType.APPLICATION_FORM_URLENCODED);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    /**
     * Method: backRcvResponse(HttpServletRequest request, HttpServletResponse response)
     */
    @Test
    public void testBackRcvResponse() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: frontRcvResponse(HttpServletRequest request, HttpServletResponse response)
     */
    @Test
    public void testFrontRcvResponse() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: successRedict(HttpServletRequest request, HttpServletResponse response)
     */
    @Test
    public void testSuccessRedict() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: query(HttpServletRequest request, HttpServletResponse response)
     */
    @Test
    public void testQuery() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: check(Long orderId)
     */
    @Test
    public void testCheck() throws Exception {
//TODO: Test goes here... 
    }


} 
