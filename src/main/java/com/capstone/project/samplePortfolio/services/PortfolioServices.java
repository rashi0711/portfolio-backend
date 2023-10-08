package com.capstone.project.samplePortfolio.services;

import com.capstone.project.samplePortfolio.entity.Transactions;
import com.capstone.project.samplePortfolio.exception.InvalidNumberOfStocks;
import com.capstone.project.samplePortfolio.exception.PortfolioNotFoundException;
import com.capstone.project.samplePortfolio.exception.StockNotFoundException;
import com.capstone.project.samplePortfolio.models.Stockdetails;
import com.capstone.project.samplePortfolio.Vo.StockTemplate;
import com.capstone.project.samplePortfolio.models.StockdetailsData;
import com.capstone.project.samplePortfolio.dto.PortfolioRequest;
import com.capstone.project.samplePortfolio.dto.PortfolioResponse;
import com.capstone.project.samplePortfolio.entity.Portfolio;
import com.capstone.project.samplePortfolio.entity.Stock;
import com.capstone.project.samplePortfolio.models.TransactionsDetails;
import com.capstone.project.samplePortfolio.repository.PortfolioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

import static com.capstone.project.samplePortfolio.entity.Portfolio.SEQUENCE_NAME;

@Service
@Slf4j
public class PortfolioServices {
    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private WebClient webClient;
    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    public void addPortfolio(PortfolioRequest request, long userId) {
        long portfolioId = sequenceGeneratorService.generateSequence(SEQUENCE_NAME);
        Portfolio portfolio = Portfolio.builder()
                .portfolioId(portfolioId)
                .portfolioName(request.getPortfolioName())
                .investmentAgenda(request.getInvestmentAgenda())
                .createdAt(new Date())
                .updatedAt(new Date())
                .userID(userId)
                .stocks(new ArrayList<>())
                .build();
        portfolioRepository.save(portfolio);
    }

    public List<PortfolioResponse> getAllPortfolio(long userId) {
        List<Portfolio> portfolioList = portfolioRepository.findAll();
        List<Portfolio> userPortfolio = portfolioList.stream().filter(portfolio -> portfolio.getUserID() == userId).collect(Collectors.toList());
        return userPortfolio.stream().map(portfolio -> PortfolioResponse.builder()
                .portfolioId(portfolio.getPortfolioId())
                .portfolioName(portfolio.getPortfolioName())
                .investmentAgenda(portfolio.getInvestmentAgenda())
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .stocks(portfolio.getStocks())
                .userID(portfolio.getUserID())
                .build()).collect(Collectors.toList());

    }

    public PortfolioResponse getPortfolioById(long portfolioId) throws PortfolioNotFoundException {
        Optional<Portfolio> portfolio = portfolioRepository.findById(portfolioId);
        if (portfolio.isPresent()) {
            PortfolioResponse portfolioResponse = PortfolioResponse.builder()
                    .portfolioId(portfolio.get().getPortfolioId())
                    .portfolioName(portfolio.get().getPortfolioName())
                    .investmentAgenda(portfolio.get().getInvestmentAgenda())
                    .createdAt(portfolio.get().getCreatedAt())
                    .updatedAt(portfolio.get().getUpdatedAt())
                    .userID(portfolio.get().getUserID())
                    .stocks(portfolio.get().getStocks())
                    .build();
            return portfolioResponse;
        }
        throw new PortfolioNotFoundException("Portfolio with id " + portfolioId + " not found");
    }

    public void deletePortfolio(long portfolioId) throws PortfolioNotFoundException {
        Optional<Portfolio> portfolio = portfolioRepository.findById(portfolioId);
        if (portfolio.isPresent()) {
            portfolioRepository.deleteById(portfolioId);
            portfolio.map(value -> PortfolioResponse.builder()
                    .portfolioId(value.getPortfolioId())
                    .portfolioName(value.getPortfolioName())
                    .investmentAgenda(value.getInvestmentAgenda())
                    .createdAt(value.getCreatedAt())
                    .updatedAt(value.getUpdatedAt())
                    .userID(value.getUserID())
                    .stocks(value.getStocks())
                    .build()
            );
            return;
        }

        throw new PortfolioNotFoundException("Portfolio with id " + portfolioId + " not found");


    }

    public PortfolioResponse updatePortfolio(long portfolioId, PortfolioRequest portfolio) throws PortfolioNotFoundException {
        Optional<Portfolio> portfolio1 = portfolioRepository.findById(portfolioId);
        if (portfolio1.isPresent()) {
            portfolio1.get().setPortfolioId(portfolioId);
            portfolio1.get().setPortfolioName(portfolio.getPortfolioName());
            portfolio1.get().setInvestmentAgenda(portfolio.getInvestmentAgenda());
            portfolio1.get().setUpdatedAt(new Date());
            portfolio1.get().setUserID(portfolio1.get().getUserID());
            portfolio1.get().setCreatedAt(portfolio1.get().getCreatedAt());
            portfolio1.get().setStocks(portfolio1.get().getStocks());
            portfolioRepository.save(portfolio1.get());
            return PortfolioResponse.builder()
                    .portfolioId(portfolioId)
                    .portfolioName(portfolio1.get().getPortfolioName())
                    .investmentAgenda(portfolio1.get().getInvestmentAgenda())
                    .createdAt(portfolio1.get().getCreatedAt())
                    .updatedAt(portfolio1.get().getUpdatedAt())
                    .userID(portfolio1.get().getUserID())
                    .stocks(portfolio1.get().getStocks())
                    .build();
        } else {
            throw new PortfolioNotFoundException("Portfolio with id " + portfolioId + " not found");
        }

    }


    public void buyStock(int numberOfStocks, String stockId, long portfolioId) throws PortfolioNotFoundException, StockNotFoundException {

        Optional<Portfolio> portfolio=portfolioRepository.findById(portfolioId);
        if(portfolio.isPresent()){
            List<StockTemplate> stocks=webClient.get()
                    .uri("http://localhost:8080/stock/getAll/"+stockId)
                    .retrieve()
                    .bodyToFlux(StockTemplate.class).collectList().block();
            StockTemplate stock=stocks.get(0);
            if(stock!=null){
                List<Stock> stocksList=portfolio.get().getStocks();
                Optional<Stock> previousStock=stocksList.stream().filter(data->data.getStockId().equals(stockId)).findFirst();
                if(previousStock.isEmpty()){
                    List<Transactions>transactionsList=new ArrayList<>();
                    Transactions transactions=new Transactions("buy",new Date(),numberOfStocks,numberOfStocks*stock.getCurrentPrice());
                    transactionsList.add(transactions);
                    Stock newStock=new Stock(stockId,stock.getName(),stock.getImage(),stock.getSymbol(),numberOfStocks,stock.getCurrentPrice(),0.0,stock.getCurrentPrice(),transactionsList);
                    stocksList.add(newStock);
                }else{
                    double totalBuy=previousStock.get().getTotalBuy()+(stock.getCurrentPrice()*previousStock.get().getQuantity());
                    double totalSell=previousStock.get().getTotalSell();
                    double avgBuyPrice=totalBuy/(numberOfStocks+previousStock.get().getQuantity());
                    List<Transactions> transactionsList=previousStock.get().getTransactions();
                    Transactions transactions=new Transactions("Buy",new Date(),numberOfStocks,numberOfStocks*stock.getCurrentPrice());
                    transactionsList.add(transactions);
                    Stock newStock=new Stock(stockId,stock.getName(),stock.getImage(),stock.getSymbol(),numberOfStocks+previousStock.get().getQuantity(),totalBuy,totalSell,avgBuyPrice,transactionsList);
                    stocksList.remove(previousStock.get());
                    stocksList.add(newStock);
                }
                portfolio.get().setStocks(stocksList);
                portfolioRepository.save(portfolio.get());
            }else{
                throw new StockNotFoundException("Stock does not exist");
            }

        }else{
            throw new PortfolioNotFoundException("Portfolio with id " + portfolioId + " not found");
        }
    }

    public StockdetailsData getAllStocks(long portfolioId) throws PortfolioNotFoundException {
        double returns = 0;
        double balance = 0;
        String stockIds="";
        Optional<Portfolio> portfolio = portfolioRepository.findById(portfolioId);
        List<Stockdetails> stockList = new ArrayList<>();
        StockdetailsData stockdetailsList = new StockdetailsData();

        if (portfolio.isPresent()) {
            List<Stock> portfolioStocksList = portfolio.get().getStocks();
            for(Stock s:portfolioStocksList) {
                  stockIds += s.getStockId() + ",";
              }
           if(stockIds.length()>0){
                  String str=stockIds.substring(0,stockIds.length()-1);
                  List<StockTemplate> templates=webClient.get()
                          .uri("http://localhost:8080/stock/getAll/"+str)
                          .retrieve()
                          .bodyToFlux(StockTemplate.class).collectList().block();
                  for(Stock s:portfolioStocksList) {
                      Stockdetails stockDetails = new Stockdetails();
                      stockDetails.setStock(s);
                      List<StockTemplate> stockTemplateList= templates.stream().filter(t->t.getId().equals(s.getStockId())).collect(Collectors.toList());
                      StockTemplate stockTemplate=stockTemplateList.get(0);
                      double holdings=stockTemplate.getCurrentPrice()*s.getQuantity();
                      stockDetails.setHoldings(holdings);
                      stockDetails.setReturns(holdings-(s.getTotalBuy()-s.getTotalSell()));
                      stockDetails.setCurrentPrice(stockTemplate.getCurrentPrice());
                      stockDetails.setPriceChangePercentage24h(stockTemplate.getPriceChangePercentage24h());
                      stockDetails.setPriceChange24h(stockTemplate.getPriceChange24h());
                      stockList.add(stockDetails);
                  }
                  for (Stockdetails stockDetails : stockList) {
                      returns += stockDetails.getReturns();
                      balance += stockDetails.getHoldings();
                  }

                  stockdetailsList.setStockDetails(stockList);
                  stockdetailsList.setTotalReturns(returns);
                  stockdetailsList.setTotalBalance(balance);
              }

            return stockdetailsList;

        } else {
            throw new PortfolioNotFoundException("Portfolio with id " + portfolioId + " not found");
        }
    }

    public void sellStock(int quantity, String stockId, long portfolioId) throws PortfolioNotFoundException, InvalidNumberOfStocks, StockNotFoundException {
        Optional<Portfolio> portfolio=portfolioRepository.findById(portfolioId);
        if(portfolio.isPresent()){
            List<StockTemplate> stocks=webClient.get()
                    .uri("http://localhost:8080/stock/getAll/"+stockId)
                    .retrieve()
                    .bodyToFlux(StockTemplate.class).collectList().block();
            StockTemplate stock=stocks.get(0);
            if(stock!=null){
                List<Stock> stocksList=portfolio.get().getStocks();
                Optional<Stock> previousStock=stocksList.stream().filter(data->data.getStockId().equals(stockId)).findFirst();
                int previousStockQuantity=previousStock.get().getQuantity();
                if(previousStock.isPresent()){
                  if(previousStockQuantity>=quantity){
                      int updatedQuantity=previousStockQuantity-quantity;
                      double totalSell=previousStock.get().getTotalSell()+(stock.getCurrentPrice()*quantity);
                      List<Transactions> transactionsList=previousStock.get().getTransactions();
                      Transactions transactions=new Transactions("Sell",new Date(),quantity,quantity*stock.getCurrentPrice());
                      transactionsList.add(transactions);
                      Stock newStock=new Stock(stockId,stock.getName(),stock.getImage(),stock.getSymbol(),updatedQuantity,previousStock.get().getTotalBuy(),totalSell,previousStock.get().getAvgBuyPrice(),transactionsList);
                      stocksList.remove(previousStock.get());
                      stocksList.add(newStock);
                  }
                  else{
                      throw new InvalidNumberOfStocks("Invalid Number of stocks");
                  }
                    portfolio.get().setStocks(stocksList);
                    portfolioRepository.save(portfolio.get());
                } else {
                    throw new StockNotFoundException("Stock not found");
                }
            } else {
                throw new StockNotFoundException("Stock not found");
            }
        } else {
            throw new PortfolioNotFoundException("Portfolio with id " + portfolioId + " not found");
        }
    }

    public StockTemplate getAllStocksDetails(String stockId) {
        return webClient.get().uri("http://localhost:8080/stock/" + stockId).retrieve()
                .bodyToMono(StockTemplate.class).block();
    }

    public TransactionsDetails getTransactionsHistory(long portfolioId, String stockId) {
        Optional<Portfolio> portfolio=portfolioRepository.findById(portfolioId);
        if(portfolio.isPresent()){
            TransactionsDetails transactionsDetails=new TransactionsDetails();
            List<Stock> stockList=portfolio.get().getStocks();
            Optional<Stock> stock=stockList.stream().filter(data->data.getStockId().equals(stockId)).findFirst();
            if(stock.isPresent()){
                List<StockTemplate> stocks=webClient.get()
                        .uri("http://localhost:8080/stock/getAll/"+stockId)
                        .retrieve()
                        .bodyToFlux(StockTemplate.class).collectList().block();
                StockTemplate stockTemplate=stocks.get(0);
                double holdings=stockTemplate.getCurrentPrice()*stock.get().getQuantity();
                List<Transactions> transactionsList=stock.get().getTransactions();
                transactionsDetails.setStock(stock.get());
                transactionsDetails.setTransactionsDetailsList(transactionsList);
                transactionsDetails.setHoldings(holdings);
                transactionsDetails.setReturns(holdings-(stock.get().getTotalBuy()-stock.get().getTotalSell()));
                return transactionsDetails;
            }
            return null;
        }
        return null;
    }
}