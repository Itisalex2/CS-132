package model;

import java.util.HashMap;
import java.util.Map;

public class TranslationContext {
  Map<String, FunctionContext> functionContexts = new HashMap<>();

  public void addFunctionContext(String functionName, FunctionContext context) {
    functionContexts.put(functionName, context);
  }

  public FunctionContext getFunctionContext(String functionName) {
    return functionContexts.get(functionName);
  }

  @Override
  public String toString() {
    return "TranslationContext{" +
        "functionContexts=" + functionContexts +
        '}';
  }
}
