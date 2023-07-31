
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.crio.warmup.stock.portfolio.PortfolioManagerImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.apache.logging.log4j.ThreadContext;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;
import java.time.temporal.ChronoUnit;

public class PortfolioManagerApplication{

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  //  Task:
  //       - Read the json file provided in the argument[0], The file is available in the classpath.
  //       - Go through all of the trades in the given file,
  //       - Prepare the list of all symbols a portfolio has.
  //       - if "trades.json" has trades like
  //         [{ "symbol": "MSFT"}, { "symbol": "AAPL"}, { "symbol": "GOOGL"}]
  //         Then you should return ["MSFT", "AAPL", "GOOGL"]
  //  Hints:
  //    1. Go through two functions provided - #resolveFileFromResources() and #getObjectMapper
  //       Check if they are of any help to you.
  //    2. Return the list of all symbols in the same order as provided in json.

  //  Note:
  //  1. There can be few unused imports, you will need to fix them to make the build pass.
  //  2. You can use "./gradlew build" to check if your code builds successfully.


  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {

    ObjectMapper om = getObjectMapper();
    List<String> list = new ArrayList<>();
    PortfolioTrade trades[] = om.readValue(resolveFileFromResources(args[0]), PortfolioTrade[].class);
    for(PortfolioTrade trade: trades){
      list.add(trade.getSymbol());
    }
    return list;
  }


  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.



  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>



  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  //  Follow the instructions provided in the task documentation and fill up the correct values for
  //  the variables provided. First value is provided for your reference.
  //  A. Put a breakpoint on the first line inside mainReadFile() which says
  //    return Collections.emptyList();
  //  B. Then Debug the test #mainReadFile provided in PortfoliomanagerApplicationTest.java
  //  following the instructions to run the test.
  //  Once you are able to run the test, perform following tasks and record the output as a
  //  String in the function below.
  //  Use this link to see how to evaluate expressions -
  //  https://code.visualstudio.com/docs/editor/debugging#_data-inspection
  //  1. evaluate the value of "args[0]" and set the value
  //     to the variable named valueOfArgument0 (This is implemented for your reference.)
  //  2. In the same window, evaluate the value of expression below and set it
  //  to resultOfResolveFilePathArgs0
  //     expression ==> resolveFileFromResources(args[0])
  //  3. In the same window, evaluate the value of expression below and set it
  //  to toStringOfObjectMapper.
  //  You might see some garbage numbers in the output. Dont worry, its expected.
  //    expression ==> getObjectMapper().toString()
  //  4. Now Go to the debug window and open stack trace. Put the name of the function you see at
  //  second place from top to variable functionNameFromTestFileInStackTrace
  //  5. In the same window, you will see the line number of the function in the stack trace window.
  //  assign the same to lineNumberFromTestFileInStackTrace
  //  Once you are done with above, just run the corresponding test and
  //  make sure its working as expected. use below command to do the same.
  //  ./gradlew test --tests PortfolioManagerApplicationTest.testDebugValues

  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/bajajap12-ME_QMONEY_V2/qmoney/bin/main/trades.json";
     String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@5542c4ed";
     String functionNameFromTestFileInStackTrace = "mainReadFile()";
     String lineNumberFromTestFileInStackTrace = "29";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }



  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  
  public static String getToken(){
    return "02b2fa74427810218a6414055495dbc2793ec69e";
    // return "b75bff2e00a8d36cd423708192801ad3ea3d68ca";
  }

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    LocalDate endDate = LocalDate.parse(args[1]);
    List<String> list = new ArrayList<>();
    List<PortfolioTrade> trades = readTradesFromJson(args[0]);
    List<TotalReturnsDto> returnsDto = new ArrayList<>();

    for(PortfolioTrade trade:trades){
      List<Candle> tiingoCandle = fetchCandles(trade, endDate, getToken());
      returnsDto.add(new TotalReturnsDto(trade.getSymbol(),tiingoCandle.get(tiingoCandle.size()-1).getClose()));
    }

    Collections.sort(returnsDto);
    for(TotalReturnsDto dto : returnsDto){
      list.add(dto.getSymbol());
    }
    return list;
  }

  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {

    ObjectMapper om = getObjectMapper();
    List<PortfolioTrade> list = new ArrayList<>();
    PortfolioTrade trades[] = om.readValue(resolveFileFromResources(filename), PortfolioTrade[].class);
    for(PortfolioTrade trade: trades){
      list.add(trade);
    }
    return list;
    //return Collections.emptyList();
  }


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
//     return Collections.emptyList();
    String date = String.valueOf(endDate);
    String url = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol()+"/prices?startDate=" + 
        trade.getPurchaseDate() +"&endDate=" + date + "&token=" + token;
    return url;
}

  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  public static Double getOpeningPriceOnStartDate(List<Candle> candles) {
     return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
     return candles.get(candles.size()-1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    String url = prepareUrl(trade, endDate, token);
    RestTemplate restTemplate = new RestTemplate();
    TiingoCandle tiingoCandle[] = restTemplate.getForObject(url, TiingoCandle[].class);
    return Arrays.asList(tiingoCandle);
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
        List<PortfolioTrade> trades = readTradesFromJson(args[0]);
        List<AnnualizedReturn> list = new ArrayList<>();
        LocalDate endDate = LocalDate.parse(args[1]);

        for(PortfolioTrade trade: trades){
          List<Candle> candles = fetchCandles(trade, endDate, getToken());
          Double buyPrice = getOpeningPriceOnStartDate(candles);
          Double sellPrice = getClosingPriceOnEndDate(candles);
          AnnualizedReturn annualizedReturn = calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
          list.add(annualizedReturn);
        }
        
        Collections.sort(list);
        return list;
     
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
    
    Double years = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate) / 365.0;
    Double totalReturn = (sellPrice-buyPrice)/buyPrice;
    Double annualizedReturn = Math.pow((1+totalReturn),(1/years))-1;
    return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturn);
  }




  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
      RestTemplate restTemplate = new RestTemplate();
      PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);
      String file = args[0];
      LocalDate endDate = LocalDate.parse(args[1]);
//      String contents = readFileAsString(file);
      ObjectMapper objectMapper = getObjectMapper();
      PortfolioTrade portfolioTrades[] = objectMapper.readValue(file, PortfolioTrade[].class);
      // return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);

      return portfolioManager.calculateAnnualizedReturnParallel(Arrays.asList(portfolioTrades), endDate,3);
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    //printJsonObject(mainReadFile(args));

    //printJsonObject(mainReadQuotes(args));

    //printJsonObject(mainCalculateSingleReturn(args));

    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }

}


