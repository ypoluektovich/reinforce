#!/bin/sh

echo ""
echo "========= Building core with external tools"
./build-core.sh
(( $? == 0 )) || exit 1


echo ""
echo "========= Building core with bootstrapped Reinforce"
java -cp build/core-bootstrap/reinforce-core.jar:lib/snakeyaml-1.11.jar org.msyu.reinforce.Main build_core
(( $? == 0 )) || exit 1


echo ""
echo "========= Rebuilding core with Reinforced Reinforce"
java -cp build/core/reinforce-core.jar:lib/snakeyaml-1.11.jar org.msyu.reinforce.Main build_core
(( $? == 0 )) || exit 1


echo ""
echo "========= Building ivy with twice-Reinforced Reinforce"
java -cp build/core/reinforce-core.jar:lib/snakeyaml-1.11.jar org.msyu.reinforce.Main build_ivy
(( $? == 0 )) || exit 1


echo ""
echo "========= Building ivy with Ivy-enabled Reinforce"
java -cp build/core/reinforce-core.jar:build/ivy/reinforce-ivy.jar:lib/snakeyaml-1.11.jar:lib/ivy-2.3.0.jar org.msyu.reinforce.Main build_ivy
(( $? == 0 )) || exit 1


echo ""
echo "========= Building testing"
java -cp lib/snakeyaml-1.11.jar:lib/ivy-2.3.0.jar:lib/junit-4.11.jar:lib/hamcrest-core-1.3.jar:build/core/reinforce-core.jar:build/ivy/reinforce-ivy.jar org.msyu.reinforce.Main build_testing


echo ""
echo "========= Building core+ivy+testing with testing-enabled Reinforce"
java -cp lib/snakeyaml-1.11.jar:lib/ivy-2.3.0.jar:lib/junit-4.11.jar:lib/hamcrest-core-1.3.jar:build/core/reinforce-core.jar:build/ivy/reinforce-ivy.jar:build/testing/reinforce-junit.jar org.msyu.reinforce.Main build_core build_ivy build_testing


echo ""
echo "========= Building dist"
java -cp lib/snakeyaml-1.11.jar:lib/ivy-2.3.0.jar:lib/junit-4.11.jar:lib/hamcrest-core-1.3.jar:build/core/reinforce-core.jar:build/ivy/reinforce-ivy.jar:build/testing/reinforce-junit.jar org.msyu.reinforce.Main dist


echo ""
echo "========= Building dist again"
java -cp lib/snakeyaml-1.11.jar:lib/ivy-2.3.0.jar:lib/junit-4.11.jar:lib/hamcrest-core-1.3.jar:build/reinforce.jar org.msyu.reinforce.Main dist


exit 0