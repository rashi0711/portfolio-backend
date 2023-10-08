package com.capstone.project.samplePortfolio.controller;

import com.capstone.project.samplePortfolio.Vo.StockTemplate;
import com.capstone.project.samplePortfolio.entity.Transactions;
import com.capstone.project.samplePortfolio.exception.InvalidNumberOfStocks;
import com.capstone.project.samplePortfolio.exception.PortfolioNotFoundException;
import com.capstone.project.samplePortfolio.exception.StockNotFoundException;
import com.capstone.project.samplePortfolio.models.StockdetailsData;
import com.capstone.project.samplePortfolio.dto.PortfolioRequest;
import com.capstone.project.samplePortfolio.dto.PortfolioResponse;
import com.capstone.project.samplePortfolio.models.TransactionsDetails;
import com.capstone.project.samplePortfolio.services.PortfolioServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/portfolio")

public class PortfolioController {
    @Autowired
    private PortfolioServices portfolioServices;

    @PostMapping("/add/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addPortfolio(@RequestBody @Valid PortfolioRequest request, @PathVariable("userId") long userId){
        portfolioServices.addPortfolio(request,userId);
    }
    @GetMapping("/getAll/{userId}")
    public ResponseEntity<List<PortfolioResponse>> getAllPortfolio(@PathVariable("userId") long userId){
        return new ResponseEntity<>(portfolioServices.getAllPortfolio(userId),HttpStatus.OK);
    }
    @GetMapping("/get/{portfolioId}")
    public ResponseEntity<PortfolioResponse> getPortfolioById(@PathVariable("portfolioId") long portfolioId) throws PortfolioNotFoundException {
        return new ResponseEntity<>(portfolioServices.getPortfolioById(portfolioId),portfolioServices.getPortfolioById(portfolioId)!=null?HttpStatus.OK:HttpStatus.BAD_REQUEST);
    }
    @DeleteMapping("/delete/{portfolioId}")
    @ResponseStatus(HttpStatus.OK)
    public void deletePortfolio(@PathVariable("portfolioId") long portfolioId) throws PortfolioNotFoundException {
        portfolioServices.deletePortfolio(portfolioId);
    }

    @PutMapping("/update/{portfolioId}")
    public ResponseEntity<PortfolioResponse> updatePortfolio(@RequestBody PortfolioRequest portfolio, @PathVariable("portfolioId") long portfolioId) throws PortfolioNotFoundException {
        return new ResponseEntity<>(portfolioServices.updatePortfolio(portfolioId,portfolio),HttpStatus.OK);
    }

    @PutMapping("/buyStock/{quantity}/{stockId}/{portfolioId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void buyStock(@PathVariable("quantity") int quantity, @PathVariable("stockId") String stockId, @PathVariable("portfolioId") long portfolioId) throws PortfolioNotFoundException, StockNotFoundException {
        portfolioServices.buyStock(quantity,stockId,portfolioId);
    }
    @PutMapping("/sellStock/{quantity}/{stockId}/{portfolioId}")
    @ResponseStatus(HttpStatus.OK)
    public void sellStock(@PathVariable("quantity") int quantity, @PathVariable("stockId") String stockId, @PathVariable("portfolioId") long portfolioId) throws PortfolioNotFoundException, InvalidNumberOfStocks, StockNotFoundException {
        portfolioServices.sellStock(quantity,stockId,portfolioId);
    }
    @GetMapping("/getAllStocks/{portfolioId}")
    public ResponseEntity<StockdetailsData> getAllStocks(@PathVariable("portfolioId") long portfolioId) throws PortfolioNotFoundException {
        return new ResponseEntity<>(portfolioServices.getAllStocks(portfolioId),HttpStatus.OK);
    }
    @GetMapping("/getStockById/{id}")
    public ResponseEntity<StockTemplate> getAllStocksDetails(@PathVariable("id") String id){
        return new ResponseEntity<>(portfolioServices.getAllStocksDetails(id),HttpStatus.OK);
    }
    @GetMapping("/getTransaction/{portfolioId}/{stockId}")
    public ResponseEntity<TransactionsDetails> getTransactionsHistory(@PathVariable("portfolioId") long portfolioId, @PathVariable("stockId") String stockId){
        return new ResponseEntity<>(portfolioServices.getTransactionsHistory(portfolioId,stockId),HttpStatus.OK);
    }
}
