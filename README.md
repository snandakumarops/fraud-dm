# Setup

Install PAM
import
https://github.com/snandakumar87/static-rules (build & deploy)
https://github.com/snandakumar87/dynamic-rules (build & deploy)
https://github.com/snandakumar87/fraud-bpm.git (build & deploy)


oc project kafka
cd kafka_install
sh provision-kafka.sh
mvn clean install transaction-model
oc import-image -n openshift registry.access.redhat.com/jboss-datagrid-7/datagrid72-openshift --confirm
oc new-app --name=fraud-demo-cache \
--image-stream=datagrid72-openshift:latest \
-e INFINISPAN_CONNECTORS=hotrod \
-e CACHE_NAMES=accountCache \
-e HOTROD_SERVICE_NAME=fraud-demo-cache\
-e HOTROD_AUTHENTICATION=true \
-e USERNAME=jdguser \
-e PASSWORD=P@ssword1
mvn clean install account-repository
mvn clean install account-repository-jdg
cd cache-load-service
mvn clean fabric8:deploy
cd emitter-service
mvn clean fabric8:deploy
cd dmn-static-rules-eval
mvn clean fabric8:deploy
cd dynamic-dmn-rules-eval-preload
mvn clean fabric8:deploy
cd send-to-case-mgmt (update prop for PAM url where the workflow is deployed on the fabric8.yaml)
mvn clean fabric8:deploy





