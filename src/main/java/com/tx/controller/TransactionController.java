package com.tx.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.tx.model.Transaction;
import com.tx.repo.TransactionRepo;
import com.tx.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/transaction")
public class TransactionController {
	@Autowired
	private TransactionRepo transactionRepo;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private RestTemplate restTemplate;
	
	private String getUser(HttpServletRequest request)
	{
		String token=request.getHeader("Authorization").substring(7);
		return jwtUtil.extractUsername(token);
	}
	
	@PostMapping("/send")
	public ResponseEntity<?> sendMoney(HttpServletRequest request, @RequestParam String to, @RequestParam Double amount)
	{
		String from = getUser(request);

        // 1. Deduct from sender wallet
        ResponseEntity<Map> senderResp = restTemplate.postForEntity(
            "http://localhost:8083/wallet/topup?amount=" + (-amount),
            new HttpEntity<>(null, getHeaders(request)),
            Map.class
        );

        // 2. Add to receiver wallet
        ResponseEntity<Map> receiverResp = restTemplate.postForEntity(
            "http://localhost:8083/api/wallet/topup?amount=" + amount,
            new HttpEntity<>(null, getHeaders(requestWithUser(to, request))),
            Map.class
        );

        // 3. Save transaction
        Transaction txn = new Transaction(0,from, to, amount, LocalDateTime.now());
        transactionRepo.save(txn);

        return ResponseEntity.ok("Transfer successful!");
    }

    @GetMapping("/history")
    public ResponseEntity<?> getMyTransactions(HttpServletRequest request) {
        String user = getUser(request);
        return ResponseEntity.ok(transactionRepo.findBySender(user));
    }

    private HttpHeaders getHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", request.getHeader("Authorization"));
        return headers;
    }

    private HttpServletRequest requestWithUser(String user, HttpServletRequest request) {
        // Ideally call auth-service to impersonate `to` user via internal logic.
        // For now, assume the Authorization token allows both actions (simulate).
        return request;
    }
}
