(defproject bonews-rest "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.2.374"]
                 [enlive "1.1.6"]
                 [guangyin "0.3.0"]
                 [org.seleniumhq.selenium/selenium-java "2.48.2"]
                 [com.github.detro/phantomjsdriver "1.2.0"]
                 [clj-webdriver "0.7.2"]
                 [yesql "0.5.1"]
                 [joplin.core "0.3.5"]
                 [joplin.jdbc "0.3.5"]
                 [org.postgresql/postgresql "9.4-1201-jdbc4"]
                 [http-kit "2.1.18"]
                 [bidi "2.0.9"]
                 [hiccup "1.0.5"]
                 [hiccups "0.3.0"]
                 [rum "0.9.0"]
                 [org.clojure/clojurescript "1.9.14"]
                 [cheshire "5.6.1"]
                 [cljs-ajax "0.5.5"]]
  :plugins [[lein-cljsbuild "1.1.3"]]
  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]
  :main ^:skip-aot bonews-rest.core
  :source-paths ["src/clj" "src/cljc" "src/sql" "joplin"]
  :resource-paths ["joplin" "resources/templates" "target/cljsbuild"]
  :cljsbuild {:builds {:app {:source-paths ["src/cljs" "src/cljc"]
                             :compiler {:output-to "target/cljsbuild/public/js/app.js"
                                        :output-dir "target/cljsbuild/public/js/out"
                                        :asset-path   "js/out"
                                        :optimizations :whitespace
                                        :pretty-print  true}}}}
  :profiles {:uberjar {:aot :all}}
  ;:injections [(require 'clj.alias)
  ;             (require 'clj.queries)
  ;             (require 'clj.scraper.utils)
  ;             (require 'clj.scraper.forums)
  ;             (require 'clj.scraper.subforums)
  ;             (require 'clj.scraper.threads)
  ;             (require 'clj.scraper.replies)
  ;             (require 'clj.scraper.bulbs)
  ;             (require 'clj.scraper.users)
  ;             (require 'clj.scraper.signatures)
  ;             (require 'clj.utils.lorem-ipsum)]
  :aliases {"migrate" ["run" "-m" "bonews-rest.alias/migrate"]
            "seed" ["run" "-m" "bonews-rest.alias/seed"]
            "rollback" ["run" "-m" "bonews-rest.alias/rollback"]
            "reset" ["run" "-m" "bonews-rest.alias/reset"]
            "pending" ["run" "-m" "bonews-rest.alias/pending"]
            "create" ["run" "-m" "bonews-rest.alias/create"]})
