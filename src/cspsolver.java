
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class Main {
    private static final Map<String, BiFunction<Integer, Integer, Boolean>> validOperators = new HashMap<>();
    private static int counter = 0;
    private static boolean forwardChecking = true;

    static {
        validOperators.put(">", (a, b) -> a > b);
        validOperators.put("<", (a, b) -> a < b);
        validOperators.put("=", Integer::equals);
        validOperators.put("!", (a, b) -> !a.equals(b));
    }

    static class Variable {
        String label;
        List<Integer> domain;
        Integer assignment;
    }

    public static void main(String[] args) {
        Map<String, Variable> variablesCollection = new LinkedHashMap<>();
        List<String[]> constraintsCollection = new ArrayList<>();

        try (BufferedReader filename = new BufferedReader(new FileReader(args[0]))) {
            String lineNum;
            while ((lineNum = filename.readLine()) != null) {
                lineNum = lineNum.replaceAll("\n", "").replaceAll("[\t]+$", "");
                Variable variable = new Variable();
                variable.label = String.valueOf(lineNum.charAt(0));
                List<Integer> sampleDom = new ArrayList<>();
                for (String line : lineNum.substring(3).split(" ")) {
                    if (!line.trim().isEmpty()) {
                        sampleDom.add(Integer.parseInt(line));
                    }
                }
                variable.domain = sampleDom;
                variable.assignment = null;
                variablesCollection.put(variable.label, variable);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader filename = new BufferedReader(new FileReader(args[1]))) {
            String lineNum;
            while ((lineNum = filename.readLine()) != null) {
                lineNum = lineNum.replaceAll("\n", "").replaceAll("[\t]+$", "");
                constraintsCollection.add(new String[]{lineNum.substring(0, 1), lineNum.substring(2, 3), lineNum.substring(4)});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (args[2].toLowerCase().contains("none")) {
            forwardChecking = false;
        } else {
            forwardChecking = true;
        }

        Map<String, Integer> results = recurBacktracking(new LinkedHashMap<>(), variablesCollection, constraintsCollection, forwardChecking);
        if (results != null) {
            int iterator = 0;
            counter++;
            System.out.print(counter + ". ");
            for (Map.Entry<String, Integer> entry : results.entrySet()) { // Use Map.Entry to iterate over the map
                if (iterator == results.size() - 1) {
                    System.out.println(entry.getKey() + "=" + entry.getValue() + " solution");
                } else {
                    System.out.print(entry.getKey() + "=" + entry.getValue() + ", ");
                }
                iterator++;
            }
        }
    }

    private static Map<String, Integer> recurBacktracking(Map<String, Integer> assigned, Map<String, Variable> variablesCollection, List<String[]> constraintsCollection, boolean forwardChecking) {
        if (variablesCollection.values().stream().allMatch(variable -> variable.assignment != null)) {
            return assigned;
        }

        String var = selectUnassignedVariable(variablesCollection, constraintsCollection);
        List<List<Integer>> orderedDomain = sortDomain(variablesCollection, constraintsCollection, var);

        for (List<Integer> vals : orderedDomain) {
            for (Integer val : vals) {
                boolean flag = true;
                for (String[] cons : constraintsCollection) {
                    if (Objects.equals(cons[0], variablesCollection.get(var).label)) {
                        if (variablesCollection.get(cons[2]).assignment == null) {
                            continue;
                        } else {
                            flag = validOperators.get(cons[1]).apply(val, Integer.parseInt(variablesCollection.get(cons[2]).assignment.toString()));
                        }
                    } else if (Objects.equals(cons[2], variablesCollection.get(var).label)) {
                        if (variablesCollection.get(cons[0]).assignment == null) {
                            continue;
                        } else {
                            flag = validOperators.get(cons[1]).apply(Integer.parseInt(variablesCollection.get(cons[0]).assignment.toString()), val);
                        }
                    }
                    if (!flag) {
                        int c = 0;
                        counter++;
                        System.out.print(counter + ". ");
                        for (String i : assigned.keySet()) {
                            if (c == assigned.size() - 1) {
                                System.out.println(i + "=" + assigned.get(i) + ", " + variablesCollection.get(var).label + "=" + val + " failure");
                            } else {
                                System.out.print(i + "=" + assigned.get(i) + ", ");
                            }
                            c++;
                        }
                        if (counter >= 30) {
                            System.exit(0);
                        }
                        break;
                    }
                }

                if (flag) {
                    variablesCollection.get(var).assignment = val;
                    assigned.put(var, val);
                    Map<String, Variable> resultVariablesCollection;
                    if (forwardChecking) {
                        resultVariablesCollection = forwardCheckingFunction(copyVariables(variablesCollection), constraintsCollection, var);
                        for (Variable variable : resultVariablesCollection.values()) {
                            if (variable.domain.size() == 0) {
                                int c = 0;
                                if (counter >= 30) {
                                    System.exit(0);
                                }
                                continue;
                            }
                        }
                    } else {
                        resultVariablesCollection = variablesCollection;
                    }
                    Map<String, Integer> result = recurBacktracking(assigned, resultVariablesCollection, constraintsCollection, forwardChecking);
                    if (result != null) {
                        return result;
                    }
                    variablesCollection.get(var).assignment = null;
                    assigned.remove(var);
                }
            }
        }
        return null;
    }

    private static Map<String, Variable> forwardCheckingFunction(Map<String, Variable> variablesCollection, List<String[]> constraintsCollection, String var) {
        Integer assignedValue = variablesCollection.get(var).assignment;
        for (String[] cons : constraintsCollection) {
            if (Objects.equals(cons[0], variablesCollection.get(var).label)) {
                if (variablesCollection.get(cons[2]).assignment == null) {
                    List<Integer> removalList = new ArrayList<>();
                    for (Integer value : variablesCollection.get(cons[2]).domain) {
                        if (!validOperators.get(cons[1]).apply(assignedValue, value)) {
                            removalList.add(value);
                        }
                    }
                    variablesCollection.get(cons[2]).domain.removeAll(removalList);
                }
            } else if (Objects.equals(cons[2], variablesCollection.get(var).label)) {
                if (variablesCollection.get(cons[0]).assignment == null) {
                    List<Integer> removalList = new ArrayList<>();
                    for (Integer value : variablesCollection.get(cons[0]).domain) {
                        if (!validOperators.get(cons[1]).apply(value, assignedValue)) {
                            removalList.add(value);
                        }
                    }
                    variablesCollection.get(cons[0]).domain.removeAll(removalList);
                }
            }
        }
        return variablesCollection;
    }

    private static String selectUnassignedVariable(Map<String, Variable> variables, List<String[]> constraintsCollection) {
        String var = null;
        List<String> varList = new ArrayList<>();
        for (String v : variables.keySet()) {
            if (variables.get(v).assignment == null) {
                if (var == null) {
                    var = v;
                    varList.add(v);
                } else if (variables.get(var).domain.size() > variables.get(v).domain.size()) {
                    var = v;
                    varList = new ArrayList<>();
                    varList.add(v);
                } else if (variables.get(var).domain.size() == variables.get(v).domain.size()) {
                    final String finalVar = var; // Create a final copy of var
                    int varcount = 0;
                    int variablecount = 0;
                    for (String[] cons : constraintsCollection) {
                        if (Objects.equals(cons[0], variables.get(finalVar).label) && variables.get(cons[2]).assignment == null) {
                            varcount += constraintsCollection.stream().filter(i -> Objects.equals(i[0], variables.get(finalVar).label) && variables.get(i[2]).assignment == null).count();
                            varcount += constraintsCollection.stream().filter(i -> variables.get(i[0]).assignment == null && Objects.equals(i[2], variables.get(finalVar).label)).count();
                        } else if (variables.get(cons[0]).assignment == null && Objects.equals(cons[2], variables.get(finalVar).label)) {
                            variablecount += constraintsCollection.stream().filter(i -> Objects.equals(i[0], variables.get(v).label) && variables.get(i[2]).assignment == null).count();
                            variablecount += constraintsCollection.stream().filter(i -> variables.get(i[0]).assignment == null && Objects.equals(i[2], variables.get(v).label)).count();
                        }
                    }
                    if (varcount < variablecount) {
                        var = v;
                        varList = new ArrayList<>();
                        varList.add(v);
                    } else if (varcount == variablecount) {
                        varList.add(v);
                    }
                }
            }
        }
        return var;
    }
    
    private static List<List<Integer>> sortDomain(Map<String, Variable> variablesCollection, List<String[]> constraintsCollection, String var) {
        Map<Integer, List<Integer>> valueConstraints = new HashMap<>();
        for (Integer value : variablesCollection.get(var).domain) {
            int tempValue = 0;
            for (String[] con : constraintsCollection) {
                if (Objects.equals(con[0], variablesCollection.get(var).label) && variablesCollection.get(con[2]).assignment == null) {
                    for (Integer compValue : variablesCollection.get(con[2]).domain) {
                        if (!validOperators.get(con[1]).apply(value, compValue)) {
                            tempValue++;
                        }
                    }
                } else if (variablesCollection.get(con[0]).assignment == null && Objects.equals(con[2], variablesCollection.get(var).label)) {
                    for (Integer compValue : variablesCollection.get(con[0]).domain) {
                        if (!validOperators.get(con[1]).apply(compValue, value)) {
                            tempValue++;
                        }
                    }
                }
            }
            valueConstraints.computeIfAbsent(tempValue, k -> new ArrayList<>()).add(value);
        }
        List<List<Integer>> orderedDomain = new ArrayList<>();
        valueConstraints.keySet().stream().sorted().forEach(key -> orderedDomain.add(valueConstraints.get(key)));
        return orderedDomain;
    }

    private static Map<String, Variable> copyVariables(Map<String, Variable> original) {
        Map<String, Variable> copy = new HashMap<>();
        for (Map.Entry<String, Variable> entry : original.entrySet()) {
            Variable variable = new Variable();
            variable.label = entry.getValue().label;
            variable.domain = new ArrayList<>(entry.getValue().domain);
            variable.assignment = entry.getValue().assignment;
            copy.put(entry.getKey(), variable);
        }
        return copy;
    }
}
