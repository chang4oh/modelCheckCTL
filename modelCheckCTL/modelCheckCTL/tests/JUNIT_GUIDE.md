# JUnit 5 tests for the existing CTL Model Checker

This adds JUnit tests **without changing existing CTL algorithms or moving your production Java files**.

## Where to work

The Maven project is the **inner** `modelCheckCTL` directory containing `Main.java`, `model/`, `view/`, `controller/`, and `util/`.

From the GitHub repository root on Windows PowerShell:

```powershell
cd modelCheckCTL_GROUP_NUMBER/modelCheckCTL
java -version
mvn -version
mvn test
```

Use JDK 17+ and Apache Maven 3.9+. Maven downloads JUnit 5 the first time (internet is required).

- `pom.xml` configures Maven, JUnit 5, and the existing nonstandard source layout.
- `src/test/java/modelCheckCTL/tests/ModelCheckerTest.java` exercises temporal-logic algorithms.
- `src/test/java/modelCheckCTL/tests/ParserTest.java` checks model/CTL syntax and detailed errors.
- `src/test/java/modelCheckCTL/tests/ControllerTest.java` checks Swing controller wiring and skips if headless.
- Reports are generated under `target/surefire-reports/`.

## Test just one class

```powershell
mvn -Dtest=ModelCheckerTest test
mvn -Dtest=ParserTest test
mvn -Dtest=ControllerTest test
```

The original standalone GUI and regression scripts still work:

```powershell
java -jar modelCheckCTL.jar
java -cp build modelCheckCTL.Main --suite tests/fixtures
```

The supplied JAR is **not** automatically rebuilt by `mvn test`. To rebuild the JAR, use your established build procedure.

## GitHub updates

From the Git **repository root**, review the changes before committing:

```powershell
git status --short
git add -- .gitignore .github/workflows/ctl-junit.yml modelCheckCTL_GROUP_NUMBER/modelCheckCTL/pom.xml modelCheckCTL_GROUP_NUMBER/modelCheckCTL/JUNIT_GUIDE.md modelCheckCTL_GROUP_NUMBER/modelCheckCTL/src/test/java/modelCheckCTL/tests/ModelCheckerTest.java modelCheckCTL_GROUP_NUMBER/modelCheckCTL/src/test/java/modelCheckCTL/tests/ParserTest.java modelCheckCTL_GROUP_NUMBER/modelCheckCTL/src/test/java/modelCheckCTL/tests/ControllerTest.java
git diff --cached --stat
git commit -m "Add JUnit 5 tests and GitHub Actions for CTL checker"
git push origin main
```

**Review all staged files**. A Windows-extracted ZIP may show modified files solely from line-ending conversions; do not commit those unless intentionally changed.

GitHub Actions will automatically run `mvn test` on each push. Its remote result, not this guide, determines whether the JUnit tests passed.
