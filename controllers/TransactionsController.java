package com.app.controllers;

import com.app.dto.StatisticsDTO;
import com.app.dto.TransactionDTO;
import com.app.exceptions.NotFoundException;
import com.app.exceptions.UnprocessableException;
import com.app.service.TransactionsService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TransactionsController {

	private static final Logger LOGGER= LoggerFactory.getLogger(TransactionsController.class);

	TransactionsService transactionsService;

	@Autowired
	public TransactionsController(TransactionsService transactionsService){
		this.transactionsService=transactionsService;
	}

	@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
	@ResponseBody()
	@ExceptionHandler(InvalidFormatException.class)
	public void handleFieldsNotParsable() {
		LOGGER.error("Some fields are not parsable");
	}

	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ResponseBody()
	@ExceptionHandler(JsonParseException.class)
	public void handleJsonParseException() {
		LOGGER.error("The JSON request is not valid");
	}
	@RequestMapping(value="/transactions", method = RequestMethod.POST , produces = MediaType.APPLICATION_JSON_VALUE, consumes=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity save(@RequestBody TransactionDTO transactionDTO){
		HttpStatus status;
		try{
			LOGGER.info("Saving transaction....");
			transactionsService.saveTransaction(transactionDTO);
			status=HttpStatus.CREATED;
		}catch (NotFoundException e){
			status=HttpStatus.NO_CONTENT;
			LOGGER.error("The transaction is older than 60 sec.");
		}catch(UnprocessableException e){
			status=HttpStatus.UNPROCESSABLE_ENTITY;
			LOGGER.error("The transaction is in the future");
		}
		catch (Exception e){
			status=HttpStatus.INTERNAL_SERVER_ERROR;
			LOGGER.error("Internal Server Error! {}",e.getMessage());
		}
		return new ResponseEntity(status);
	}

	@RequestMapping(value="/transactions",method = RequestMethod.DELETE , produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity delete(){
		HttpStatus status;
		try {
			transactionsService.deleteAllTransactions();
			status = HttpStatus.NO_CONTENT;
		}catch (Exception e){
			status=HttpStatus.INTERNAL_SERVER_ERROR;
			LOGGER.error("Internal server error {}",e.getMessage());
		}
		return new ResponseEntity(status);
	}



	@RequestMapping(value="/statistics", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<StatisticsDTO> getTransactions(){
		StatisticsDTO responseDTO=null;
		HttpStatus status=null;
		try{
			responseDTO=transactionsService.getStatistics();
				status=HttpStatus.OK;
		}catch (Exception e){
			status=HttpStatus.INTERNAL_SERVER_ERROR;
			LOGGER.error("Internal server error {}",e.getMessage());
		}
		return new ResponseEntity<>(responseDTO,status);

	}


	
}
