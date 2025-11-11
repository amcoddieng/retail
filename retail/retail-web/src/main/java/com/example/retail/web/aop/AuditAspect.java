package com.example.retail.web.aop;

import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {
    @Around("@annotation(aud)")
    public Object around(ProceedingJoinPoint pjp, Auditable aud) throws Throwable {
        long t0 = System.currentTimeMillis();
        try {
            return pjp.proceed();
        } finally {
            System.out.println("AUDIT " + aud.value() + " took " + (System.currentTimeMillis() - t0) + "ms");
        }
    }
}