# web4ᵗʰ

## install

1. `npm install`
2. `mv src/app/.env.edn.example src/app/.env.edn`
3. get "publishable api key" from https://magic.link/ and enable "WebAuthn" in the "Passwordless login" section
4. go to https://app.element.io/#/register and change "matrix.org" to "https://matrix.teawaterwire.dev"
5. register a username for `app-id`
6. sign out and register another username for `support-id`
7. fill `.env.edn` with these values
8. `npm run dev`
9. open http://localhost:8280/
10. log in with a username and Touch ID on desktop (https://magic.link/docs/login-methods/webauthn)

## develop

it's all about adding actions:

1. create a file under `app/actions` and add it to `registry.cljs`
2. create a component that will be displayed when the action is triggered
3. register the action by implementing the multimethod `actions/get-action`
4. optionally use `actions/add-primary-action` to make it a primary action (available from the command bar)

hello world example:

```clj
;; app/actions/hello.cljs

(ns app.actions.hello
  (:require
   [app.actions.entrypoint :as actions]))

(defn c-hello []
  [:div.text-center "gm 🌞"])

(defmethod actions/get-action ::hello
  []
  {:component c-hello})

(actions/add-primary-action ::hello "Hello" {:default? true})

```

```clj
;; app/actions/registry.cljs

(ns app.actions.registry
  (:require [app.actions.onboarding]
            [app.actions.pingpong]
            [app.actions.hello]))
```

## support

to understand what is first-class support, sign in on https://app.element.io/ with the `support-id` account

then back from the application click "Start chat with support"

on the support side you can now send messages to the user, but more importantly also actions that will be displayed as components!

## about state
