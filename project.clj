(defproject bonews-rest "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [enlive "1.1.6"]
                 [guangyin "0.3.0"]
                 [org.seleniumhq.selenium/selenium-java "2.48.2"]
                 [com.github.detro/phantomjsdriver "1.2.0"]
                 [clj-webdriver "0.7.2"]
                 [yesql "0.5.1"]
                 [joplin.core "0.3.5"]
                 [joplin.jdbc "0.3.5"]
                 [org.postgresql/postgresql "9.4-1201-jdbc4"]]
  :main ^:skip-aot bonews-rest.core
  :target-path "target/%s"
  :source-paths ["src/clj" "joplin"]
  :resource-paths ["joplin"]
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
