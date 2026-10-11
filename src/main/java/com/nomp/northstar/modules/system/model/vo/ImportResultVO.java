package com.nomp.northstar.modules.system.model.vo;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ImportResultVO {
  private int success;
  private int failed;
  private List<String> errors = new ArrayList<>();
}
