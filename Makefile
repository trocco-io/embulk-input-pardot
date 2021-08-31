
check: g-check

build: g-build

g-%:
	./gradlew $(*)

preview: g-gem
	embulk preview -l warn -I build/gemContents/lib secret.yml

deploy-packages:
	# TODO: use variable for versions
	# gem push --key github --host https://rubygems.pkg.github.com/trocco-io build/gems/embulk-input-pardot-0.0.11-java.gem