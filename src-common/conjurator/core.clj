(ns conjurator.core
  (:require [play-clj.core :refer :all]
            [play-clj.ui :refer :all]
            [play-clj.g2d :refer :all]
            [conjurator.utils :as u]
            [conjurator.entities :as e]))

(use 'clojure.pprint)

(declare update-accel
         update-accel-player
         move
         reset-accel
         check-inputs
         fucking-print-player
         update-x-player-speed
         update-y-player-speed
         apply-ground-resistance
         get-touching-ground-tile)

(defn- print+ret [o]
  (println o)
  o)

(defn- get-direction []
  (cond
    (key-pressed? :dpad-left) :left
    (key-pressed? :dpad-right) :right
    :else nil))

(defn- get-player
  [entities]
  (first (filter :player? entities)))

(defn- jumping-key-pressed? []
  (key-pressed? :x))

(defn- assoc-can-jump [entity]
  ;; (assoc entity :can-jump? (= 0 (:y entity))))
  (assoc entity :can-jump? (<= (:y-spd entity) 0)))

(defn- update-x-movement-accel [entities]
  (if-let [direction (get-direction)]
    (map #(update-accel-player direction u/accel %) entities)
    entities))

(defn- update-accel-player [direction factor entity]
  (if (:player? entity)
    (update-accel direction factor entity)
    entity))

(defn- update-accel [direction factor entity]
  (case direction
    :left (assoc entity :x-accel (- factor))
    :right (assoc entity :x-accel factor)
    :up (assoc entity :y-accel factor)
    :down (assoc entity :y-accel (- factor))
    entity))

(defn- update-jumping-accel [entities]
  (if (and (jumping-key-pressed?)
           (:can-jump? (get-player entities)))
    (map #(update-accel-player :up u/jump-accel %) entities)
    entities))

(defn- TEMP-prevent-move [entity]
  (if (< (:y entity) 0) (assoc entity :y 0 :y-spd 0)
      entity))

(defn- reset-accel [entity]
  (assoc entity :x-accel 0 :y-accel 0))

(defn- move [entities]
  (map (fn [{:keys [player?] :as entity}]
         (if player?
           (let [{curr-x-spd :x-spd curr-y-spd :y-spd
                  x-accel :x-accel y-accel :y-accel
                  old-x :x old-y :y} entity]
             (-> entity
                 (update-x-player-speed x-accel)
                 (update-y-player-speed y-accel)
                 (update-y-player-speed u/gravity-accel)
                 (apply-ground-resistance)

                 (assoc :x (+ old-x curr-x-spd)
                        :y (+ old-y curr-y-spd))

                 (TEMP-prevent-move)
                 (assoc-can-jump)
                 (reset-accel)))
           entity))
    entities))

(defn- prevent-move [entities]
  (map (fn [{:keys [player?, x y, x-spd y-spd] :as entity}]
         (if (and player?
                  (neg? y-spd))
           (if-let [colliding-entity (e/get-colliding-entity entity entities)]
             (assoc entity
                    :y (+ (:y colliding-entity)
                          (:height colliding-entity))
                    :y-spd 0)
             entity)
           entity))
       entities))

(defn- update-x-player-speed [{curr-x-spd :x-spd :as player} x-accel]
  (cond
    (> curr-x-spd u/max-player-speed) (assoc player :x-spd u/max-player-speed)
    (< curr-x-spd (- u/max-player-speed)) (assoc player :x-spd (- u/max-player-speed))
    :else (assoc player :x-spd (+ curr-x-spd x-accel))))

(defn- update-y-player-speed [{curr-y-spd :y-spd :as player} y-accel]
  (assoc player :y-spd (+ curr-y-spd y-accel)))

(defn- apply-ground-resistance [{:keys [x-spd] :as player}]
  (cond
    (pos? x-spd) (assoc player :x-spd (- x-spd u/ground-resistance))
    (neg? x-spd) (assoc player :x-spd (+ x-spd u/ground-resistance))
    :else player))

(defn- check-inputs [entities]
  (->> entities
       (update-x-movement-accel);; TODO Maybe change this ?
       (update-jumping-accel)))

(defn- update-fps-counter [entities]
  (map (fn [{fps? :fps? :as ent}]
         (if fps?
           (doto ent (label! set-text (str (game :fps))))
           ent))
       entities))

(defn- get-touching-ground-tile
  [player entities]
  (let [{player-x :x player-y :y player-width :width player-height :height} player]
    (->> entities
         (filter #(:floor? %))
         (filter (fn [ent]
                   (and (= player-y (+ (:y ent) (:height ent) 1)))))
         (first)
         (identity))
    entities ;; TODO REMOVE THIS
  ))

(defn- fucking-print-player [entities]
  (pprint (get-player entities))
  entities)

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (update! screen :renderer (stage) :camera (orthographic))
    (update! screen :timeline [])
    (let [gab-ganon (e/create-player)
          background (e/create-background)
          fps-counter (e/create-fps-counter)
          floor (e/create-floor)
          tiles (e/create-tiles [20 20] [50 60] [100 250] [400 250])]
      (flatten [background floor tiles gab-ganon fps-counter])))

  :on-resize
  (fn [screen entities]
    (height! screen 400)
    (width! screen 800))

  :on-key-down
  (fn [screen entities]
    (cond
      (= (:key screen) (key-code :q)) (System/exit 0)
      (= (:key screen) (key-code :p)) (conj entities (assoc (texture "images/gabganon.png")
                                            :x (rand 800)
                                            :y (rand 400)
                                            :width (rand 100)
                                            :height (rand 100)))))

  :on-touch-dragged
  (fn [screen entities]
    (if-not (button-pressed? :right)
      (conj entities (e/texture-at :gabganon (:input-x screen) (- 400 (:input-y screen)) true))))

  :on-touch-down
  (fn [screen entities]
    (if (= (:button screen) (button-code :right))
      (conj entities (e/grass-at (:input-x screen) (- 400 (:input-y screen))))))

  :on-render
  (fn [screen entities]
    (clear!)
    (->> (if (key-pressed? :r)
           (rewind! screen 1)
           (->> entities
                check-inputs
                move
                prevent-move
                update-fps-counter))
          (render! screen))))

(defgame conjurator-game
  :on-create
  (fn [this]
    (music "music/YoshiTheme.mp3" :play)
    (set-screen! this main-screen)))

;;(app! :post-runnable #(set-screen! conjurator-game main-screen))
