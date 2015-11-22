(defproject bonews-rest "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [enlive "1.1.6"]
                 [guangyin "0.3.0"]
                 [org.seleniumhq.selenium/selenium-java "2.48.2"]
                 [clj-webdriver "0.7.2"]]
  :main ^:skip-aot bonews-rest.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :injections [(require 'bonews-rest.scraper.utils)
               (require 'bonews-rest.scraper.forums)
               (require 'bonews-rest.scraper.subforums)
               (require 'bonews-rest.scraper.threads)
               (require 'bonews-rest.scraper.replies)
               (require 'bonews-rest.scraper.bulbs)
               (require 'bonews-rest.scraper.signatures)])
