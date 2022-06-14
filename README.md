# web4ᵗʰ

web4ᵗʰ or "temporal web" is a paradigm where time is added as a new dimension for user interactions on the web: the fourth dimension 🔮

the rationale can be found here: https://mirror.xyz/penseur.eth/mvqOkcw0ABBgRjSD49Pb_V3lbpQMxxi68KCHabYj5RA

this repository is meant to be used as a starter kit for those willing to try web4ᵗʰ at their next company hackathon or from the comfort of their homes

## install

1. `npm install`
2. get "publishable api key" from https://magic.link/ and enable "WebAuthn" in the "Passwordless login" section
3. go to https://app.element.io/#/register and change "matrix.org" to "https://matrix.teawaterwire.dev"
4. register a username for `app-id`
5. sign out and register another username for `support-id`
6. fill `env` in `config.cljs` with these values (magic-key, app-id, support-id)
7. `npm run dev` (any Java SDK > 8 is needed)
8. open http://localhost:8280/
9. log in with a username and Touch ID on desktop (https://magic.link/docs/login-methods/webauthn)
10. profit

## develop

it's all about adding actions

### 1, 2, 3, 4ᵗʰ steps:

1. **create a file under `app/actions` and add it to `registry.cljs`**

```clj
;; action.cljs
(ns app.actions.action
  (:require
   [app.actions.entrypoint :as actions]))

;; registry.cljs
(ns app.actions.registry
  (:require [app.actions.action]]))
```

2. **create a component that will be displayed when the action is triggered**

the component is passed an initial `state`

```clj
(defn component [state]
  [:div "something: " (:something state)])
```

an action can be triggered from a component by calling `actions/send-action`

```clj
[:button {:on-click #(actions/send-action :action)}]
```

optional arguments can be passed to the action

```clj
[:button {:on-click #(actions/send-action :action arguments)}]
```

3. **register the action by implementing the multimethod `actions/get-action`**

the method is passed three values: the action, the global db and the arguments

```clj
(defmethod actions/get-action :action
  [action db args]
  {:component component
   :state {:something "something"
           :and (from-global db)
           :more (from-local args)}})
```

the global `db` contains value like the username, the messages in the timeline or anything that can be added with [re-frame](http://day8.github.io/re-frame/)

4. **optionally use `actions/add-primary-action` to make it a primary action**

an action marked as primary can be triggered directly in the command bar, with the option to make it appear by default as well

```clj
(actions/add-primary-action :action "Label" {:default? true})
```

### examples

you can find examples (hello, pingpong, todolist) under the [actions/examples](https://github.com/teawaterwire/web4th/tree/master/src/app/actions/examples) folder

just comment the line in `registry.cljs` if you want to deactivate them without deleting the code

### onboarding

there's a special action called `:app.actions.onboarding/onboarding` that is triggered automatically when the user logs in the first time

the matching component should be an introductory message for the user telling them what they can do with your app — don't waste the opportunity 😉

## state of the state

_what is state?_

- state is "data over time"

_but what is the purpose of data?_

- to be used by components

_for what?_

- to create new data upon the triggering of "actions"

_does it mean that in web4ᵗʰ state is spread over all components?_

- yes, each component represents a sliced snapshot of state

_isn't that inefficient?_

- components can have local state and it's up to developers to decide when snapshots should be taken (and trigger an action that will render another component)

_what's the point of this?_

- being able to keep and navigate to all previous important states of the application unlocks new capabilities
- for example, users can go back in time to components and trigger different actions that will "branch off" to a different present 🤯

## command bar

let's talk about the section at the bottom of the screen: the command bar

it's grouping three features:

1. start/stop chat with support as first-class citizen

2. trigger primary actions

3. input freeform text to either:
   - chat with support
   - filter through primary and non-default actions
   - just write anything like on a notebook

## support

to understand what is support as first-class citizen, sign in on https://app.element.io/ with the `support-id` account

then back from the application click "Start chat with support"

on the support side you can now send messages to the user, but more importantly also actions that will be displayed as components!

## release

using [Cloudflare Pages](https://pages.cloudflare.com/) it's as simple as connecting your GitHub repository and adding these settings:

> Framework preset: `None`
>
> build command: `npm run release`
>
> Build output directory: `/resources/public`
>
> Root directory: `/`
>
> Environment variables: `NODE_VERSION` = 16

click "Save and Deploy" and voilà 💫

## style

need help plz 🎨 (using [Tailwind](https://tailwindcss.com/) btw - it's great)

## post rationalisation

choices were made

### Magic

authentication is inevitable but has to be as frictionless as possible

a private key / public key provided by crypto wallet was the first guess, but friction was still there

using biometrics to authenticate is frictionless, albeit supported only on desktop at the moment — that's a trade-off

behind the hood Magic provides private public-key cryptography, which is used to authenticate on Matrix protocol

### Matrix

a decentralized conversation store was the key missing ingredient for a true web4ᵗʰ app

leveraging their work at the protocol level but also at the application level: existing cross platform clients can be used to interact with users (support account)

_for convenience a Matrix homeserver is deployed at matrix.teawaterwire.dev but you can deploy your own relatively easily following this helpful [tutorial](https://pcarion.com/posts/matrix-dendrite-install/) (and passing the `--really-enable-open-registration` flag)_

### ClojureScript

it feels right to use immutable data for immutable conversations

but ClojureScript has other advantages:

- true interactive programming with a REPL ([Calva](https://calva.io/) recommended)
- [re-frame](http://day8.github.io/re-frame/) to get smooth global state management
- [reagent](https://reagent-project.github.io/) for the most elegant way to write React component

people not willing to write a single lispy parenthesis can just write JavaScript (and React components) — the amazing tool which is [shadow-cljs](https://shadow-cljs.github.io/docs/UsersGuide.html#classpath-js) will deal with it 😌

---

## interested or furious?

_<3 open to contributions <3_

> Webo Webini Lupus 🐺
