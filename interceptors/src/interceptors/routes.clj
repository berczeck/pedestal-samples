(ns interceptors.routes
	(:require [io.pedestal.http.route :as route]
						[interceptors.handler :as handler]
						[clojure.data.json :as json]
						[io.pedestal.interceptor.error :as error-int]
						[io.pedestal.http.body-params :as body-params]))

(def db-interceptor
	{:name :database-interceptor
	 :enter (fn [context]
						(update context :request assoc :datomic {:conn "test"}))})

(def path-id->int
	{:name	 :path-id->int
	 :enter		(fn [{{:keys [path-params]} :request :as context}]
							 (if-let [id (:id path-params)]
								 (update context :request assoc :int-id (Integer/parseInt id))
								 context))})

(defn get-response-from-error
	[error]
	(let [type (get (ex-data error) :type)]
		(case type
			:not-found {:status 404 :body  "Custom Not found"}
			{:status 500 :body "Custom Internal server error"})))

(def service-error-handler
	(error-int/error-dispatch [context ex]
		[{:exception-type :clojure.lang.ExceptionInfo}]
		(assoc context :response (get-response-from-error ex))
		:else
		(assoc context :io.pedestal.impl.interceptor/error ex)))

(defn to->json
	[response]
	(-> response
		(update :body json/write-str)
		(assoc-in [:headers "Content-Type"] "application/json")))

(def json-body
	{:name :json-body
	 :leave (fn [context]
						(update-in context [:response] to->json))})

(def routes
	(route/expand-routes
		#{["/hello" :get [json-body handler/respond-hello] :route-name :hello]
			["/owners/:name" :post [json-body db-interceptor (body-params/body-params) handler/owner-post] :route-name :owner-post]
			["/owners/:name" :get [json-body handler/owner-get] :route-name :owner-get]
			["/greet/:id" :get [service-error-handler db-interceptor path-id->int handler/greet-get] :route-name :greet-get :constraints {:id #"[0-9]+"}]
			["/error/:id" :get [service-error-handler json-body path-id->int handler/error-get] :route-name :error-get]}))
