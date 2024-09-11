
---

# CSP Solver with Heuristics

## Description
This project is a Constraint Satisfaction Problem (CSP) solver implemented in Java. It utilizes backtracking with optional forward checking to solve CSP problems. The solver employs Minimum Remaining Value and Most Constraining Variable heuristics for variable selection, and Least Constraining Value heuristic for value selection. The program reads input files specifying variables, domains, and constraints, and outputs the search tree branches or the solution.

## Requirements
- **Programming Language**: Java
- **Input Files**: 
  - `.var` file: Contains the variables in the CSP to be solved and their domains. Each line of the file contains a variable (represented by a single letter), followed by a colon and its possible values, each of which is an integer.
  - `.con` file: Contains the constraints. Each line corresponds to exactly one constraint, which involves two variables and has the form `VAR1 OP VAR2`. `VAR1` and `VAR2` are the names of the two variables involved, and `OP` can be one of four binary operators: `=` (equality), `!` (inequality), `>` (greater than), and `<` (less than).
- **Consistency-Enforcing Procedure**: Can take one of two values: `none` and `fc`. If `none` is used, no consistency-enforcing procedure is applied, and the solver simply uses backtracking to solve the problem. `fc` indicates that the solver will use forward checking to enforce consistency.

## Usage
To run the program, follow these steps:

1. **Compile the Java file**:
   ```sh
   javac cspsolver.java
   ```

2. **Execute the compiled file and pass in exactly three arguments in the order listed above**:
   ```sh
   java cspsolver <.var file> <.con file> <fc/none>
   ```

   Example commands:
   ```sh
   java cspsolver ex1.var ex1.con fc   # With Forward Checking
   java cspsolver ex1.var ex1.con none # Without Forward Checking
   ```

## Output
The program writes to stdout the branches visited in the search tree or stops when a solution is reached. Branches represent assignments that violate constraints or lead to one or more unassigned variables having empty domains (when using forward checking).

### Sample Output
With `.var` containing:
```
A: 1 2 3 4 5
B: 1 2 3 4 5
C: 1 2 3 4 5
D: 1 2 3 4 5
E: 1 2 3
F: 1 2
```
and `.con` containing:
```
1. A > B
2. B > F
3. A > C
4. C > E
5. A > D
6. D = E
```
The output would be:
- **With Forward Checking**:
  ```
  1. F=1, E=1, D=1, A=5, B=2, C=2 solution
  ```
- **Without Forward Checking**:
  ```
  1. F=1, E=1, A=5, B=1 failure
  2. F=1, E=1, A=5, B=2, C=1 failure
  3. F=1, E=1, A=5, B=2, C=2, D=1 solution
  ```

## Heuristics
- **Variable Selection**: Minimum Remaining Value heuristic is applied, breaking ties using the Most Constraining Variable heuristic. If multiple variables remain, they are selected alphabetically.
- **Value Selection**: Least Constraining Value heuristic is applied. If multiple values remain, smaller values are preferred.

## Sample Input Files
- `ex1.var`
- `ex1.con`

## Files to Check Solution
- `ex1-fc.out`
- `ex1-none.out`

---
