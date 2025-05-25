package visitor;

import java.util.HashMap;
import java.util.Map;

import IR.token.Identifier;
import model.FastLivelinessModel;
import sparrow.Add;
import sparrow.Alloc;
import sparrow.Block;
import sparrow.Call;
import sparrow.ErrorMessage;
import sparrow.FunctionDecl;
import sparrow.Goto;
import sparrow.IfGoto;
import sparrow.Instruction;
import sparrow.LabelInstr;
import sparrow.LessThan;
import sparrow.Load;
import sparrow.Move_Id_FuncName;
import sparrow.Move_Id_Id;
import sparrow.Move_Id_Integer;
import sparrow.Multiply;
import sparrow.Print;
import sparrow.Program;
import sparrow.Store;
import sparrow.Subtract;
import sparrow.visitor.ArgVisitor;

public class FastLivelinessVisitor implements ArgVisitor<String> {
  FastLivelinessModel model = new FastLivelinessModel();
  Map<String, Integer> labelMap = new HashMap<>();
  int currentLine = 0;

  public FastLivelinessModel getFastLivelinessModel() {
    return model;
  }

  @Override
  public void visit(Program n, String arg) {
    for (FunctionDecl fd : n.funDecls) {
      fd.accept(this, arg);
    }
  }

  @Override
  public void visit(FunctionDecl n, String arg) {
    String funcName = n.functionName.toString();

    currentLine = 1;
    labelMap.clear();

    for (Identifier fp : n.formalParameters) {
      model.putDef(funcName, fp.toString(), 0);
      model.putUse(funcName, fp.toString(), -1);
    }

    n.block.accept(this, funcName);
  }

  @Override
  public void visit(Block n, String funcName) {
    for (Instruction i : n.instructions) {
      i.accept(this, funcName);
      currentLine++;
    }

    model.putUse(funcName, n.return_id.toString(), currentLine);
  }

  @Override
  public void visit(LabelInstr n, String funcName) {
    labelMap.put(n.label.toString(), currentLine);
  }

  @Override
  public void visit(Move_Id_Integer n, String funcName) {
    model.putDef(funcName, n.lhs.toString(), currentLine);
    model.putUse(funcName, n.lhs.toString(), -1);
  }

  @Override
  public void visit(Move_Id_FuncName n, String funcName) {
    model.putDef(funcName, n.lhs.toString(), currentLine);
    model.putUse(funcName, n.lhs.toString(), -1);
  }

  @Override
  public void visit(Add n, String funcName) {
    model.putDef(funcName, n.lhs.toString(), currentLine);
    model.putUse(funcName, n.lhs.toString(), -1);
    model.putUse(funcName, n.arg1.toString(), currentLine);
    model.putUse(funcName, n.arg2.toString(), currentLine);
  }

  @Override
  public void visit(Subtract n, String funcName) {
    model.putDef(funcName, n.lhs.toString(), currentLine);
    model.putUse(funcName, n.lhs.toString(), -1);
    model.putUse(funcName, n.arg1.toString(), currentLine);
    model.putUse(funcName, n.arg2.toString(), currentLine);
  }

  @Override
  public void visit(Multiply n, String funcName) {
    model.putDef(funcName, n.lhs.toString(), currentLine);
    model.putUse(funcName, n.lhs.toString(), -1);
    model.putUse(funcName, n.arg1.toString(), currentLine);
    model.putUse(funcName, n.arg2.toString(), currentLine);
  }

  @Override
  public void visit(LessThan n, String funcName) {
    model.putDef(funcName, n.lhs.toString(), currentLine);
    model.putUse(funcName, n.lhs.toString(), -1);
    model.putUse(funcName, n.arg1.toString(), currentLine);
    model.putUse(funcName, n.arg2.toString(), currentLine);
  }

  @Override
  public void visit(Load n, String funcName) {
    model.putDef(funcName, n.lhs.toString(), currentLine);
    model.putUse(funcName, n.lhs.toString(), -1);
    model.putUse(funcName, n.base.toString(), currentLine);
  }

  @Override
  public void visit(Store n, String funcName) {
    model.putUse(funcName, n.base.toString(), currentLine);
    model.putUse(funcName, n.rhs.toString(), currentLine);
  }

  @Override
  public void visit(Move_Id_Id n, String funcName) {
    model.putDef(funcName, n.lhs.toString(), currentLine);
    model.putUse(funcName, n.lhs.toString(), -1);
    model.putUse(funcName, n.rhs.toString(), currentLine);
  }

  @Override
  public void visit(Alloc n, String funcName) {
    model.putDef(funcName, n.lhs.toString(), currentLine);
    model.putUse(funcName, n.lhs.toString(), -1);
    model.putUse(funcName, n.size.toString(), currentLine);
  }

  @Override
  public void visit(Print n, String funcName) {
    model.putUse(funcName, n.content.toString(), currentLine);
  }

  @Override
  public void visit(ErrorMessage n, String funcName) {
  }

  @Override
  public void visit(Goto n, String funcName) {
    String label = n.label.toString();
    int labelLine = labelMap.getOrDefault(label, -1);
    if (labelLine != -1 && labelLine < currentLine) {
      Map<String, Integer> defMap = model.getDefMapForFunc(funcName);
      Map<String, Integer> useMap = model.getUseMapForFunc(funcName);

      for (Map.Entry<String, Integer> entry : defMap.entrySet()) {
        String var = entry.getKey();
        int defLine = entry.getValue();
        int useLine = useMap.get(var);
        if (defLine < labelLine && useLine > labelLine) {
          model.putUse(funcName, var, currentLine);
        }
      }
    }
  }

  @Override
  public void visit(IfGoto n, String funcName) {
    model.putUse(funcName, n.condition.toString(), currentLine);
    String label = n.label.toString();
    int labelLine = labelMap.getOrDefault(label, -1);
    if (labelLine != -1 && labelLine < currentLine) {
      Map<String, Integer> defMap = model.getDefMapForFunc(funcName);
      Map<String, Integer> useMap = model.getUseMapForFunc(funcName);

      for (Map.Entry<String, Integer> entry : defMap.entrySet()) {
        String var = entry.getKey();
        int defLine = entry.getValue();
        int useLine = useMap.get(var);
        if (defLine < labelLine && useLine > labelLine) {
          model.putUse(funcName, var, currentLine);
        }
      }
    }
  }

  @Override
  public void visit(Call n, String funcName) {
    model.putDef(funcName, n.lhs.toString(), currentLine);
    model.putUse(funcName, n.lhs.toString(), -1);
    model.putUse(funcName, n.callee.toString(), currentLine);
    for (Identifier arg : n.args) {
      model.putUse(funcName, arg.toString(), currentLine);
    }
  }
}
