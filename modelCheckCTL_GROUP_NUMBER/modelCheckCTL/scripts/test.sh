#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash scripts/build.sh
java -cp build modelCheckCTL.Main --suite tests/fixtures
java -cp build modelCheckCTL.Main --batch tests/microwave/microwave.txt tests/microwave/microwave-formulas.txt
# Verify every malformed example is rejected. The error messages must include a line number.
for i in {1..8}; do
  if java -cp build modelCheckCTL.Main --check "tests/fixtures/Broken Model $i.txt" s1 'EX p' > /tmp/ctl-broken-model.log 2>&1; then
    echo "FAIL: Broken Model $i was accepted"; exit 1
  fi
  if ! grep -q 'Line ' /tmp/ctl-broken-model.log; then
    echo "FAIL: Broken Model $i has no line number"; exit 1
  fi
  echo "Broken Model $i: rejected with line number"
done
for expr in 'Gp' 'AGAp' 'AGp or not' 'EFGr' 'A not G not p' 'F(r U q)' 'AEFt'; do
  if java -cp build modelCheckCTL.Main --check 'tests/fixtures/Model 1.txt' s1 "$expr" >/tmp/ctl-invalid.log 2>&1; then
    echo "FAIL: Invalid formula accepted: $expr"; exit 1
  fi
  if ! grep -q 'Column ' /tmp/ctl-invalid.log; then
    echo "FAIL: No column info: $expr"; exit 1
  fi
  echo "Invalid formula $expr: rejected"
done
# The supplied GUI tests cover both positive and negative results.
java -cp build modelCheckCTL.Main --check 'tests/fixtures/Model 1.txt' s1 'EXq'
java -cp build modelCheckCTL.Main --check 'tests/fixtures/Model 1.txt' s2 'EXq'
# Check nonexistent state is rejected.
if java -cp build modelCheckCTL.Main --check 'tests/fixtures/Model 1.txt' s99 'EFp' >/tmp/ctl-state.log 2>&1; then
  echo 'FAIL: nonexistent state accepted'; exit 1
fi
javac -encoding UTF-8 -cp build -d build tests/DetailedResults.java
java -cp build modelCheckCTL.tests.DetailedResults tests docs/DetailedTestResults.csv
echo 'ALL TEST GROUPS PASSED'
