
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.PortfolioManagerApplication;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuoteServiceFactory;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.crio.warmup.stock.quotes.TiingoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  private RestTemplate restTemplate;
  private StockQuotesService stockQuotesService;
  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate; 
  }

  protected PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
  }

  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF

  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate){
        List<AnnualizedReturn> list = new ArrayList<>(); 
        for(PortfolioTrade trade: portfolioTrades){
          List<Candle> candles = null;
          try {
            candles = stockQuotesService.getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);
          }catch (JsonProcessingException e) {
            e.printStackTrace();
          }catch(StockQuoteServiceException exception){
            System.out.println(exception.getMessage());
          }catch(RuntimeException exception){
            exception.printStackTrace();
          }
          Double buyPrice = PortfolioManagerApplication.getOpeningPriceOnStartDate(candles);
          Double sellPrice = PortfolioManagerApplication.getClosingPriceOnEndDate(candles);
          Double years = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate) / 365.0;
          Double totalReturn = (sellPrice-buyPrice)/buyPrice;
          Double annualizedReturn = Math.pow((1+totalReturn),(1/years))-1;
      
          list.add(new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturn));
        }
        
        Collections.sort(list, getComparator());
        return list;
  }



  private static Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  
  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
        String url = buildUri(symbol, from, to);
        TiingoCandle candles[] = restTemplate.getForObject(url, TiingoCandle[].class);        
        return Arrays.asList(candles);
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
      //  String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
      //       + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
      String token = PortfolioManagerApplication.getToken();
      String start = String.valueOf(startDate);
      String end = String.valueOf(endDate);
      String uriTemplate = "https://api.tiingo.com/tiingo/daily/"+symbol+"/prices?startDate="
      +start+"&endDate="+end+"&token="+token;

      //02b2fa74427810218a6414055495dbc2793ec69e
      return uriTemplate;
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturnParallel(
      List<PortfolioTrade> portfolioTrades, LocalDate endDate, int numThreads)
      throws InterruptedException, StockQuoteServiceException {
    // TODO Auto-generated method stub
    ExecutorService pool = Executors.newFixedThreadPool(numThreads);
    // long startTime = System.currentTimeMillis();
    
    List<Future<AnnualizedReturn>> futureReturnsList = new ArrayList<Future<AnnualizedReturn>>();
    List<AnnualizedReturn> list = new ArrayList<>();
    for(PortfolioTrade trade: portfolioTrades){
    
      Callable<AnnualizedReturn> task = () -> {
        return getSingleAnnualizedReturn(trade, endDate);
      };

      Future<AnnualizedReturn> future = pool.submit(task);
      
      futureReturnsList.add(future);
    }
  
//    Collections.sort(futureReturnsList,getComparator());
    long endTime = System.currentTimeMillis();
    for(Future<AnnualizedReturn> annual : futureReturnsList){
      try{
        AnnualizedReturn newObject = annual.get();
        // System.out.println(newObject.getAnnualizedReturn());
        list.add(newObject);
      }catch(ExecutionException exc){
        throw new StockQuoteServiceException("Stock Quote....");
      }
    }
    // System.out.println(endTime-startTime);
    // System.out.println(list);
    Collections.sort(list,getComparator());
    return list;
  
  }

  public AnnualizedReturn getSingleAnnualizedReturn(PortfolioTrade trade, LocalDate endDate){
    
    List<Candle> candles = null;
    try {
      candles = stockQuotesService.getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);
    }catch (JsonProcessingException e) {
      e.printStackTrace();
    }catch(StockQuoteServiceException exception){
      System.out.println(exception.getMessage());
    }catch(RuntimeException exception){
      exception.printStackTrace();
    }
    Double buyPrice = PortfolioManagerApplication.getOpeningPriceOnStartDate(candles);
    Double sellPrice = PortfolioManagerApplication.getClosingPriceOnEndDate(candles);
    Double years = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate) / 365.0;
    Double totalReturn = (sellPrice-buyPrice)/buyPrice;
    Double annualizedReturn = Math.pow((1+totalReturn),(1/years))-1;
    
    // System.out.println(new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturn));
    return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturn);

  }
}
