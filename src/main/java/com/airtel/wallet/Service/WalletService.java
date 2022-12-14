package com.airtel.wallet.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.airtel.wallet.DAO.Response;
import com.airtel.wallet.Entity.Wallet;
import com.airtel.wallet.Repository.WalletRepository;


@Service
public class WalletService {

	@Autowired
	WalletRepository repo;

	//Check Balance for given ID
	public Optional<Wallet> checkBalance(Integer id) {
		return repo.findById(id);
	}

	//Credit given amount to given userID
	@Transactional(isolation=Isolation.SERIALIZABLE)
	public ResponseEntity<Response> creditAmount(Integer id, Long amount) {
		Response r = new Response();
		Optional<Wallet> wall = repo.findById(id);

		if (!wall.isPresent()) { 
			r.setErrorCode(0);
			r.setMessage("Transaction Not Successfull");
			return new ResponseEntity<Response>(r, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		Long bal = wall.get().getBalance();
		bal = bal + amount;
		wall.get().setBalance(bal);

		repo.save(wall.get());
		r.setErrorCode(1);
		r.setMessage("Transaction Successfull " + "Balance: " + bal);
		return new ResponseEntity<Response>(r, HttpStatus.OK);

	}

	//Debit amount from given userId
	@Transactional(isolation=Isolation.SERIALIZABLE)
	public ResponseEntity<Response> debitAmount(Integer id, Long amount) {
		Response r = new Response();
		Optional<Wallet> wall = repo.findById(id);

		if (!wall.isPresent()) {
		
			r.setErrorCode(0);
			r.setMessage("Transaction Not Successfull");
			return new ResponseEntity<Response>(r, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		Long bal = wall.get().getBalance();
		if (bal < amount) {
			r.setErrorCode(0);
			r.setMessage("Insufficient Balance");
			return new ResponseEntity<Response>(r, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		bal = bal - amount;
		wall.get().setBalance(bal);

		repo.save(wall.get());
		r.setErrorCode(1);
		r.setMessage("Transaction Successfull " + "Balance:"+bal);
		return new ResponseEntity<Response>(r, HttpStatus.OK);

	}

	//add new User
	public ResponseEntity<Response> addUser(Wallet wallet) {
		Response r = new Response();
		Optional<Wallet> wall = repo.findById(wallet.getId());
		if(wall.isPresent())
		{
			r.setErrorCode(1);
			r.setMessage("User ID already exists.");
			return new ResponseEntity<Response>(r, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		try {
			if (wallet.getBalance() > 0) {
				repo.save(wallet);		
				r.setErrorCode(1);
				r.setMessage("Added Successfully");
				return new ResponseEntity<Response>(r, HttpStatus.OK);
			} else {
				r = new Response();
				r.setErrorCode(0);
				r.setMessage("Unable to add user");
				return new ResponseEntity<Response>(r, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			
			r.setErrorCode(0);
			r.setMessage("Unable to add user");
			return new ResponseEntity<Response>(r, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	//transfer amount from sender to receiver
	@Transactional(isolation=Isolation.SERIALIZABLE)
	public ResponseEntity<Response> transferMoney(Integer senderId, Integer receiverID, Long amount) 
	{
		Response r = new Response();
		
		Optional<Wallet> senderwallet = repo.findById(senderId);
		Optional<Wallet> receiverwallet = repo.findById(receiverID);
		//Debit
		if (!senderwallet.isPresent() || !receiverwallet.isPresent()) {
		
			r.setErrorCode(0);
			r.setMessage("Transaction Not Successfull");
			return new ResponseEntity<Response>(r, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		Long senderbal = senderwallet.get().getBalance();
		if (senderbal < amount) {
			r.setErrorCode(0);
			r.setMessage("Insufficient Balance");
			return new ResponseEntity<Response>(r, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		senderbal = senderbal - amount;
		senderwallet.get().setBalance(senderbal);

		repo.save(senderwallet.get());
		
		//Credit

		Long receiverbal = receiverwallet.get().getBalance();
		receiverbal = receiverbal + amount;
		receiverwallet.get().setBalance(receiverbal);

		repo.save(receiverwallet.get());
		r.setErrorCode(1);
		r.setMessage("Transaction Successfull " + "Sender Balance: " + senderbal + " Sender Balance: "+receiverbal);
		return new ResponseEntity<Response>(r, HttpStatus.OK);

	}
}
