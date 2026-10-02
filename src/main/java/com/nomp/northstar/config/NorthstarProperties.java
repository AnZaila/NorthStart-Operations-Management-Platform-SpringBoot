package com.nomp.northstar.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "northstar")
public class NorthstarProperties {
  private Cors cors = new Cors();
  private Security security = new Security();
  private Storage storage = new Storage();

  @Data
  public static class Cors {
    private List<String> origins = new ArrayList<>();
  }

  @Data
  public static class Security {
    private int maxLoginFailures = 5;
    private int lockMinutes = 15;
    private int captchaAfterFailures = 3;
    private int passwordMinLength = 8;
    private int passwordMaxLength = 32;
    private int passwordHistory = 3;
    private int refreshTtlDays = 7;
  }

  @Data
  public static class Storage {
    private String dir = "./data/uploads";
  }
}
