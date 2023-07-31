
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.PortfolioManagerApplication;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {

  private RestTemplate restTemplate;

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException,StockQuoteServiceException{
    // TODO Auto-generated method stub
      List<Candle> list = new ArrayList<>();
      String url = buildUri(symbol, from, to);
      String response;
      try{
        response = restTemplate.getForObject(url, String.class);
        if(response==null){
          throw new StockQuoteServiceException("No record is found");
        }
      }catch(RuntimeException exception){
        throw exception;
      }
      
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      TiingoCandle[] candles = mapper.readValue(response, TiingoCandle[].class);
      if(candles!=null){
        list = Arrays.asList(candles);
      }else{
        list = Arrays.asList(new TiingoCandle[0]);
      }
      return list;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    //  String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
    //       + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
    String start = String.valueOf(startDate);
    String end = String.valueOf(endDate);
    String token = PortfolioManagerApplication.getToken();
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/"+symbol+"/prices?startDate="
    +start+"&endDate="+end+"&token="+token;

    return uriTemplate;
}


  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest


  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.





  // TODO: CRIO_TASK_MODULE_EXCEPTIONS
  //  1. Update the method signature to match the signature change in the interface.
  //     Start throwing new StockQuoteServiceException when you get some invalid response from
  //     Tiingo, or if Tiingo returns empty results for whatever reason, or you encounter
  //     a runtime exception during Json parsing.
  //  2. Make sure that the exception propagates all the way from
  //     PortfolioManager#calculateAnnualisedReturns so that the external user's of our API
  //     are able to explicitly handle this exception upfront.

  //CHECKSTYLE:OFF


}
