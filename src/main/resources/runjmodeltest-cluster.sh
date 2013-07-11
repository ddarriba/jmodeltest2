export MPJ_HOME=$PWD/mpj
export NP=$1
shift
$MPJ_HOME/bin/mpjrun.sh -dev niodev -wdir $PWD/ -np $NP -jar jModelTest.jar  $*
