package com.company.MakeMyTrip.pricing_service.auth;




import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;


@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private final HttpServletRequest request;

    public FeignClientInterceptor(HttpServletRequest request) {
        this.request = request;
    }


    @Override
    public void apply(RequestTemplate requestTemplate) {
        Long userId= UserContextHolder.getCurrentUserId();

        if(userId!=null){
            requestTemplate.header("X-User-Id",userId.toString());
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            requestTemplate.header("Authorization", authHeader);
        }
    }
}
