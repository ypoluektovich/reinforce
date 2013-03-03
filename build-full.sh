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

exit 0