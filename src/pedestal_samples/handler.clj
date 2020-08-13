(ns pedestal-samples.handler)

(defn respond-hello [request]
	{:status 200 :body "Hello, world!"})

(defn owner-post [{:keys [json-params datomic]}]
	{:status 200 :body "Ok"})

(defn owner-get [{:keys [path-params datomic]}]
	{:status 200 :body (str "Owner " (:name path-params))})

(defn greet-get
	[{:keys [int-id db]}]
	{:status 200 :body {:message (str "Greetings!!! " int-id) :conn db}})

(defn error-get
	[{:keys [int-id db]}]
	(if (= int-id 1)
		(throw (ex-info "Not found" {:type :not-found}))
		{:status 200 :body {:response :ok}}))
