(ns priya.language
  "####  This namespace encapsulates all text presented when communicating with the app user.  This includes:
  - legal text  (Accept Terms of Use)
  - background notification text (e.g. fertile window prediction arrived)
  - alert text for user errors (e.g. incorrect login password)
  - alert text for other errors (e.g. wifi sync failed)
  - informational text (e.g. how to create a cycle)
  - confirmation text for irreversible actions (e.g. complete a cycle)"
  (:require [priya.db :as db]
            [clojure.string :as str]))

(def ^:private moment (js/require "moment"))

(defn- lines
  "Formats a collection of strings for display as a group."
  [xs]
  (str/join "\n" xs))

(def ^:private text-constants
  {
   :support-phone
   {:description "The Priya support phone number"
    :english     "(833) 386-5222"}

   :priya-support-email
   {:description "The email address for support questions."
    :english     "support@prima-temp.com"}

   :priya-faq-url
   {:description "The url for the Priya FAQ page."
    :english     "https://priyaring.com/faqs-1/"}

   :priya-privacy-policy-url
   {:description "The url for the Priya Privacy Policy."
    :english     "https://priyaring.com/privacy-policy"}

   :priya-privacy-policy-url-pretty
   {:description "The url for the Priya Privacy Policy, shortened for display text."
    :english     "priyaring.com/privacy-policy"}
   })

(def ^:private emails
  {
   :terms-and-conditions-email-text
   {:description "Email subject and body template for when user has questions regarding the terms and conditions."
    :english     {:subject "Regarding Terms and Conditions"
                  :body    "I have a question about the terms and conditions:\n\n[ENTER YOUR QUESTION HERE]"}}
   })

(def ^:private defaults
  {
   :oops
   {:description "A friendly alert title for user data entry errors.  Used as default"
    :english     "Oops..."}

   :default-okay-text
   {:description "Default text on the button of an alert."
    :english     "OK"}

   :default-text-cancel-confirmed-action
   {:description "Text on the button to cancel."
    :english     "Cancel"}

   :default-text-do-confirmed-action
   {:description "Text on the button to proceed with confirmed action."
    :english     "Proceed"}

   :notification-alert-title
   {:description "Title of the alert modal for notifications"
    :english     "Important Information"}

   :default-save-text
   {:description "Default text for saving actions."
    :english     "Save"}

   :default-done-text
   {:description "Default text for saving actions."
    :english     "Done"}

   :default-cancel-text
   {:description "Default text for cancelling actions."
    :english     "Cancel"}

   :default-yes
   {:description "Default text for answering 'Yes'."
    :english     "Yes"}

   :default-no
   {:description "Default text for answering 'No'."
    :english     "No"}
   })

(def ^:private date-time-formats
  "These are date-time formatting strings.  
  See https://momentjs.com/docs/#/displaying/format for the list of available date-time formats."
  {
   :human-readable-time-format
   {:description "Date and time format for technical data display"
    :english     "M/D h:mm:ssA"}

   :human-readable-day-format
   {:description "Default date format for display"
    :english     "MMM. Do"}

   :period-date-selector-format
   {:description "The format for selecting period start and end date"
    :english     "YYYY-MM-DD"}

   :period-date-display-format
   {:description "The format for dsiplaying period start and end date."
    :english     "MMM D, YY"}

   :date-of-birth-selector-format
   {:description "The format used in the date selector component"
    :english     "MMMM Do, YYYY"}

   :time-axis-formats
   {:description "Time formats for the temp graph when temperatures have been recorded for a short, medium, or long period of time."
    :english     {:short  "h:mma"
                  :medium "ha"
                  :long   "MMM D"}}

   :event-time-selector-formats
   {:description "The time formats that go in the event time selector."
    :english     {:day-only     "MMMM Do"
                  :hour-of-day  "ha"
                  :day-and-hour "MMMM Do, ha"}}

   :event-list-time-display-format
   {:description "The date-time format for time of an event in 'My Results' list"
    :english     "M/D ha"}

   :last-synced-time-format
   {:description "Format for text describing when the last ring sync occurred."
    :english "h:mma"}
   })

(def ^:private notifications
  {
   :ring-expired
   {:description "Notification message to present when the ring has expired."
    :english     "Looks like your ring was first activated over 90 days ago.  This ring is only approved to work for 90 days, so unfortunately we have to stop pairing with it.  Please follow the instructions from the user manual to remove the ring and complete your cycle."}

   :fertile-window
   {:description "Notification message to present when an ovulation prediction is received."
    :english     "Your fertile window prediction has arrived!"}

   :temp-update-without-cloud-sync
   {:description "Notification message to present when there is a bluetooth temperature sync but no accompanying cloud sync."
    :english     "You just received temperatures from the ring but they didn't save to the cloud, which means you could miss a fertile window prediction.\n\n  To fix this, just make sure you have either wifi or cellular data enabled."}

   :too-long-since-bluetooth-sync
   {:description "Notification message to present when it has been too long since the last bluetooth sync."
    :english     "It's been too long since your phone and ring have synced over bluetooth.  Your ring attempts to sync about every 2 hours but may miss out if your bluetooth is off, or if your phone is too far away, or if the Priya app is not running (Priya running in the background is okay).  To avoid losing data, keep your phone near your body until the next sync comes in, and consult the user manual if you are still having trouble.\n\n Thanks!  - the Priya Team."}

   :ring-will-soon-expire
   {:description "Notification message to present when the ring will soon expire."
    :english     "Looks like your ring was first activated almost 88 days ago.  This ring will stop collecting temperatures after 90 days, and we just wanted to give you an advanced notice."}

   :remind-to-complete-cycle
   {:description "Notification message: to present as a reminder when it is time for the user to complete her cycle."
    :english     "Judging from when you started this cycle, it looks like it's time to tap \"Complete Cycle\" in the Priya app side menu.\n\n  Thanks!"}
   })

(def ^:private confirmation-dialogs
  {
   :create-account-while-cycle-active
   {:description "Confirmation text to present when a user attempts to create and login to a different account while they have an active cycle."
    :english     {:title        "Are You Sure?"
                  :message      (str "You have a cycle that isn't completed on the account with email "
                                     @db/email*
                                     ".\n\n You can only record cycles from one account at a time,"
                                     " so creating a new account will cause you to lose that active cycle.")
                  :confirm-text "Create New Account"}}

   :login-new-account-while-cycle-active
   {:description "Confirmation text to present when a user attempts to switch accounts while they have an active cycle."
    :english     {:title        "Are You Sure?"
                  :message      (str "You have a cycle that isn't completed on the account with email " @db/email*
                                     ".\n\n You can only record cycles from one account at a time,"
                                     " so logging in with a different email will cause you to lose that active cycle.")
                  :confirm-text "Login Anyway"}}

   :reset-pairing
   {:description "Confirmation text to present when a user attempts to reset ring pairing during an active cycle."
    :english     {:title        "Wait! Are You Sure?"
                  :message      "(1) Reasons to reset pairing:\n If your ring has powered off (maybe by accidental exposure to a magnet), you will no longer be paired. Tapping \"Reset Pairing\" will allow you to re-pair. There is no other reason to reset pairing.\n\n (2) What happens next if you reset pairing?\n The \"Pair Ring\" button will re-appear and you will be able to pair in the usual way, that is:  place the ring in the cradle, tap the \"Pair Ring\" button, then after at least 3 seconds remove the ring from the cradle."
                  :confirm-text "Reset Pairing"}}

   :complete-cycle
   {:description "Confirmation text to present when a user attempts to complete the active cycle."
    :english     {:title        "Done For This Month?"
                  :message      "If you're done collecting temperatures for this month, and you have entered all of your cycle notes, tap \"Complete\".  This will stop temperature collection and make your cycle read-only."
                  :confirm-text "Complete"}}
   })

(def ^:private alerts
  {
   :please-accept-location-permission
   {:description "Instructions for: user did not give permission for the app to collect coarse location."
    :english     {:title   "Bluetooth requires location permission."
                  :message "Sorry, Android requires the location permission for any app that uses Bluetooth.\n Please restart the app and accept the location permission, or else ring pairing will not work.\n\n Thanks!"}}

   :privacy-policy-link-failed
   {:description "Linking to the Prima-Temp privacy policy failed."
    :english     {:title   "Darn"
                  :message "Sorry, the privacy policy page couldn't be opened.  Are you sure your phone has internet access?  Please try this link again or open your browser and search for Priya Privacy Policy.\n\n -Thanks!"}}

   :faq-link-failed
   {:description "Linking to the Prima-Temp faq page failed."
    :english     {:title   "Darn"
                  :message "Sorry, the FAQ page couldn't be opened.  Are you sure your phone has internet access?  Please try this link again or open your browser and search for Priya FAQ.\n\n -Thanks!"}}

   :support-phone-call-failed
   {:description "Initiating a support phone call failed."
    :english     {:title   "Oops..."
                  :message "That phone call didn't go through. Sorry for the inconvenience!
                       Please call the number directly and let us know about your question."}}

   :support-email-link-failed
   {:description "Linking to an auto-filled support email failed."
    :english     {:title   "Oops..."
                  :message "Sorry, our app wasn't able to start a support email for you.\n Please email us directly at support@prima-temp.com and let us know about your question.\n\n -Thanks!"}}

   :create-account-email-in-use
   {:description "Instructions for: user attempted to create account with an email already in use."
    :english     {:title   "Oops!"
                  :message "Looks like that email address is already in use. You can either login with this email from the login page, or create a new account with a different email address."}}

   :remote-create-account-failed
   {:description "Instructions for: user attempted to create account but the API call did not succeed."
    :english     {:title   "Hmmm..."
                  :message "Creating your account in the cloud didn't work. Do you have WiFi or Cell data enabled?"}}

   :basic-info-invalid
   {:description "User did not completely fill out the basic info form."
    :english     {:title   "Oops...",
                  :message "Looks like you're missing some information!"}}

   :login-pre-validation-invalid
   {:description "Instructions for: user failed login pre-validation"
    :english     {:title   "Oops..."
                  :message "Make sure you filled in both an email and a password."}}

   :login-local-email-correct-but-password-incorrect
   {:description "User is logging in as the same user they previously logged in as, but supplied an incorrect password."
    :english     {:title   "Oops..."
                  :message "That password doesn't match the email you entered."}}

   :login-local-success-remote-failed
   {:description "Instructions for: user logged in with email and password that matches their local storage data.  This triggers a cloud sync, but the sync did not succeed."
    :english     {:title   "Login Success!... but no cloud sync"
                  :message "You logged in successfully but we couldn't get a cloud sync.  No big deal, just make sure your wifi or cellular data is enabled and the sync will be taken care of automatically."}}

   :remote-save-on-logout-failed-allowing
   {:description "Instructions for: user logged out, which triggers a remote save, and this save did not succeed."
    :english     {:title   "Data Not Saved To Cloud"
                  :message "We always try to save off your data to the cloud when you logout, but this save didn't go through. (Maybe you have no WiFi or cell data?) Don't worry, the info is still on your phone, but you should login some time when you are on WiFi to get it saved in the cloud."}}

   :remote-login-incorrect-creds-and-no-local-user
   {:description "User is logging in to an existing account on a new device (or after having re-installed the app).  This is necessarily a remote login, but the user has supplied an incorrect email/password combination for the cloud login."
    :english     {:title   "Login Failed"
                  :message "Sorry, your email and password combination doesn't match anything in our database."}}

   :remote-login-incorrect-creds-and-different-local-user
   {:description "User is logging in as a different user.  This is necessarily a remote login, but the user has supplied an incorrect email/password combination for the cloud login."
    :english     {:title   "Login Failed"
                  :message "This is a different email than you used last time, so we checked our cloud database.\n\n That check didn't go through.  Please make sure your device has internet access, and that your email and password are correct."}}

   :remote-login-api-call-failed
   {:description "User is logging in to an existing account on a new device (or after having re-installed the app).  This is necessarily a remote login, but the api-call failed."
    :english     {:title   "Login Failed"
                  :message "Sorry, we weren't able to reach our cloud database to log you in.  Are you sure you have wifi or cellular data enabled?"}}

   :cycle-setup-and-pair-instructions
   {:description "Instructions for: the user started a new cycle."
    :english     {:title   "Your Next Steps"
                  :message "A. Set your period start date\n B. As soon as your period ends, set your period end date and pair the ring that day.\n\n Note: The ring instruction leaflet shows you how to pair, but if you want a quick reminder, here goes:\n (1) Tap the Pair button.\n (2) Place your ring in the cradle and leave it there for at least 3 seconds.\n (3) Remove your ring from the cradle and enter the pairing code if prompted.\n\n Done!"}}

   :enter-period-start
   {:description "Message for when user attempts to pair before setting period start date"
    :english     {:title   "Oops..."
                  :message "Please enter your period start date first."}}

   :set-period-dates-before-pairing
   {:description "Message for when user attempts to pair before setting period start and end dates"
    :english     {:title   "Oops..."
                  :message "Please enter your period start and end dates before trying to pair."}}

   :cycle-info-invalid
   {:description "Instructions for: user attempted to complete cycle but had not entered all information."
    :english     {:title   "Looking For Instruction?"
                  :message "Completing the cycle should be done after you've finished collecting temperatures for the month.\n\n It looks like you haven't done that yet, so start by entering your period start and end dates (when you know them), and on the day your period ends pair the ring as directed in the instructions."}}

   :pairing-too-long-after-period-end
   {:description "Message to display if user attempts to pair too many days after period has ended."
    :english     {:title   "Sorry"
                  :message "It looks like your period ended more than 2 days ago. We can't start recording temperatures this late because the fertile window prediction may not be accurate."}}

   :phone-bluetooth-is-off
   {:description "Instructions for: user's bluetooth is off."
    :english     {:title   "Oops..."
                  :message "Looks like your bluetooth is off. Please make sure it is turned on in settings and enabled."}}

   :ring-discovered-but-pairing-failed
   {:description "Instructions for: the ring was discovered, but pairing failed."
    :english     {:title   "Bluetooth Found Your Ring But Didn't Pair"
                  :message "Looks like the ring didn't pair successfully, so let's try again (please read all these steps before starting):\n\n  (1) Tap the Pair button.\n  (2) Place your ring in the cradle and wait at least 3 seconds.\n (3) Remove your ring from the cradle and enter the pairing code if prompted.\n "}}

   :scan-timed-out-without-finding-ring
   {:description "Instructions for: bluetooth scan timed out without discovering a ring."
    :english     {:title   "Bluetooth Didn't Find Your Ring"
                  :message "We scanned for your ring but never found it.  Make sure your bluetooth is on and let's try again:\n\n (1) Wait 30 seconds.\n  (2) Tap the pair button.\n   (3) Place your ring in the cradle and leave it there for at least 3 seconds.\n (4) Remove your ring from the cradle and enter the pairing code if prompted.\n "}}

   :incorrect-pairing-code
   {:description "Instructions for: the ring was discovered, but Android bluetooth bond was denied."
    :english     {:title   "Please Check Your Pairing Code and Try to Pair Again."
                  :message "It looks like either you entered the incorrect pairing code, or accidentally cancelled the pairing code entry.\n\n Here's how to fix the problem:\n (1) Double check the pairing code that came with your Priya Ring.\n  (2) Tap the Pair button.\n (3) Place your ring in the cradle and wait at least 3 seconds.\n (4) Remove your ring from the cradle and enter the pairing code if prompted.\n "}}

   :successful-ring-pairing
   {:description "Instructions for: the ring was successfully paired... what to do next."
    :english     {:title   "That Worked!"
                  :message "Great job, you successfully paired your ring!  What next?\n  (1) Insert your ring as directed in the instruction manual.\n (2) Keep your phone near you and leave the bluetooth on for the duration of your cycle.\n (3) Keep the Priya app running (it is fine to have it running in the background).\n  (4) When your temperatures indicate a fertile window, you'll receive a notification.\n  (5) If you're a data addict like we are, go ahead and check out your temperature graph from time to time.  It's pretty cool.\n Note: Rest easy... if anything isn't going right with bluetooth syncing or cloud syncing, we'll let you know and tell you how to fix the problem!"}}

   :cycle-completed
   {:description "Instructions for: cycle was completed successfully, what the user should do next."
    :english     {:title   "Cycle Complete!"
                  :message "We hope this month's experience was a good one. Please remember to remove your ring and power it down if you haven't already.\n  When you are ready to start a new cycle (this should be within 48 hours of your period ending), just tap \"New Cycle\" in the right side menu.\n\n Thanks! - The Priya Ring Team"}}
   })

(def ^:private composite-alerts
  {
   :create-account-invalid-email
   {:description "During account creation, user entered an invalid email address"
    :english     "That doesn't look like a valid email address.\n"}

   :create-account-invalid-password
   {:description "During account creation, user entered an invalid password"
    :english     "Your password should be at least six characters.\n"}

   :create-account-invalid-subject-id
   {:description "During account creation, user entered an invalid subject id"
    :english     "Subject id should be six characters provided by your trial administrator.\n"}

   :create-account-must-accept-terms
   {:description "During account creation, user failed to accept the Terms of Use"
    :english     "Please tap \"Accept Terms of Use\" to continue."}

   :create-account-empty-fields
   {:description "During account creation, user left some fields empty"
    :english     "Looks like you left some fields empty!"}
   })

(def ^:private text
  {
   :start
   {:description "Text on the start button."
    :english     "Start"}

   :terms-of-use-title
   {:description "Title for accept terms of use page"
    :english     "Priya Terms and Conditions of Use"}

   :i-do-not-accept
   {:description "Do not Accept terms and conditions checkbox text"
    :english     "Do Not Accept"}

   :i-accept
   {:description "Accept terms and conditions checkbox text"
    :english     "I Accept"}

   :accept-terms-of-use
   {:description "Text on account creation page to view and accept terms of use."
    :english     "Accept Terms of Use"}

   :tap-to-select-date
   {:description "In basic info page, text inviting user to tap to select a date of birth."
    :english     "Tap to select..."}

   :datepicker-confirm-button-text
   {:description "Text to save the entered date of birth."
    :english     "Save"}

   :datepicker-cancel-button-text
   {:description "Text to cancel date of birth entry"
    :english     "Cancel"}

   :temp-graph-time-label
   {:description "Label on temp graph x-axis when less than a day has passed."
    :english     "Time of Day"}

   :temp-graph-fertile-window
   {:description "Text to display in the fertile window area of the graph."
    :english     "Fertile\n Window"}

   :cycle-last-synced-text
   {:description "Text to inform user when the last sync with Ring occurred"
    :english     (fn [time-format]
                   (if @db/too-long-since-sync*
                    (str "\u2718 " " last synced " (-> (moment. @db/last-synced*) (.fromNow)))
                    (str "\u2714 last synced at " (-> (moment. @db/last-synced*) (.format time-format)))))}

   :cycle-notes-title
   {:description "Title for Cycle Notes text input area"
    :english     "Cycle Notes"}

   :cycle-notes-placeholder
   {:description "Placeholder for Cycle Notes text input area"
    :english     "Enter personal notes about your cycle here..."}

   :technical-data-page-title
   {:description "Title on technical data page"
    :english     "Technical Data"}

   :add-a-result-page-title
   {:description "Title on 'Add a result' modal"
    :english     "Add A Result"}

   :reset-ring?
   {:description "Text next to reset ring pairing button"
    :english     "Reset Ring?"}

   :reset-ring-pairing-button-text
   {:description "Text on the button that resets ring pairing"
    :english     "Reset Ring Pairing"}

   :return-to-cycles-page
   {:description "Text on the button that takes user back to the 'My Cycles' page."
    :english     "Back To Cycles"}

   :current-cycle-title
   {:description "Title for the current cycle"
    :english     "Current Cycle"}

   :past-cycle-title
   {:description "Title for past cycles"
    :english     (fn [past-cycle day-format] (str "Cycle Starting"
                                                  " "
                                                  (-> (moment. (:period-start past-cycle))
                                                      (.format day-format))))}

   :period-start-button-text
   {:description "Text in the button to set/display period start."
    :english     (fn [date-display-format]
                   (if @db/period-start*
                     (str "Period Start\n "
                          (-> (moment. @db/period-start*) (.format date-display-format)))
                     "Set\n Period\n Start"))}

   :period-end-button-text
   {:description "Text in the button to set/display period start."
    :english     (fn [date-display-format]
                   (if @db/period-end*
                     (str "Period End\n "
                          (-> (moment. @db/period-end*) (.format date-display-format)))
                     "Set\n Period\n End"))}

   :pair-with-ring-button-text
   {:description "Text in the button to initiate pairing, and once paired, to view fertile window status"
    :english     (condp = @db/stage
                   :scanning "Scanning..."
                   :pairing "Pairing..."
                   :paired (str "Fertile\n Window?\n"
                                (cond
                                  (= :clinical @db/mode*) "N/A"
                                  (not @db/fertile-window*) "-not yet-"
                                  (< (system-time) (:start @db/fertile-window*)) "Soon!"
                                  (< (:start @db/fertile-window*) (system-time) (:end @db/fertile-window*)) "NOW :)"
                                  (< (:end @db/fertile-window*) (system-time)) "-passed-"))
                   "Pair\n With\n Ring")}

   :notes-input-title
   {:description "Title for notes input in add result view"
    :english     "Notes"}

   :notes-input-placeholder
   {:description "Placeholder for notes input in add result view"
    :english     "[Optional] Personal notes about this result..."}


   :notes-list-title
   {:description "Title for notes is notes list"
    :english     "Notes"}

   :my-results-title-text
   {:description "Title of cycle results sections"
    :english     "My Results"}

   :add-result-button-text
   {:description "Text on the button for adding results."
    :english     "Add a result"}

   :no-temperature-readings-past-cycle
   {:description "Text in a completed cycle if no temperatures were recorded"
    :english     "No temperature readings were recorded in this cycle.  Was there a problem? Let us know!"}

   :update-profile-button-text
   {:description "Text on the button for updating profile"
    :english     "Update Profile"}

   :update-account-button-text
   {:description "Text on the button for updating account information"
    :english     "Update Account"}

   :complete-cycle-button-text
   {:description "Text on the button that completes a cycle"
    :english     "Complete Cycle"}

   :new-cycle-button-text
   {:description "Text on the button that starts a new cycle"
    :english     "New Cycle"}

   :technical-data-button-text
   {:description "Text on the button that displays the technical data view"
    :english     "Technical Data"}

   :help-button-text
   {:description "Text on the button that links user to the FAQ website."
    :english     "Help"}

   :logout-button-text
   {:description "Text on the button that logs the user out."
    :english     "Logout"}

   :cycles-page-title
   {:description "Title of the `My Cycles` page"
    :english     "My Cycles"}

   :create-account-button-text
   {:description "Text on the button to Create Account"
    :english     "Create Account"}

   :login-button-text
   {:description "Text on the button to Login"
    :english     "Login"}

   :back-button-text
   {:description "Text on the button that takes user to previous page."
    :english     "Wait, take me back!"}

   :app-title
   {:description "App title"
    :english     "Priya"}

   :app-title-page-description
   {:description "Short subtitle for App loading page"
    :english     "personal fertility sensor"}

   :loading-text
   {:description "Text to display when a page is loading"
    :english     "Loading..."}

   :create-account-page-title
   {:description "Title on create account page"
    :english     "Create Account"}

   :create-account-page-subtitle
   {:description "Sub-title on create account page"
    :english     "let's get to know each other!"}

   :first-name-input
   {:description "First name input"
    :english     {:title       "First Name"
                  :placeholder "Enter your first name..."}}

   :last-name-input
   {:description "Last name input"
    :english     {:title       "Last Name"
                  :placeholder "Enter your last name..."}}

   :subject-id-input
   {:description "Subject Id input"
    :english     {:title       "Subject Id"
                  :placeholder "Enter the subject id provided to you..."}}

   :create-password-input
   {:description "Create password input"
    :english     {:title       "Password"
                  :placeholder "Create a Priya account password..."}}

   :email-input
   {:description "Email input"
    :english     {:title       "Email"
                  :placeholder "Enter your email address..."}}

   :password-input
   {:description "Password"
    :english     {:title       "Password"
                  :placeholder "Enter your Priya account password..."}}

   :update-profile-page-title
   {:description "Title on update profile page"
    :english     "Update My Profile"}

   :basic-info-page-title
   {:description "Title on basic info page"
    :english     "Basic Info"}

   :basic-info-page-subtitle
   {:description "Sub-title on basic info page"
    :english     "Just some basic info please..."}

   :date-of-birth-question
   {:description "Text when asking date of birth"
    :english     "Your date of birth?"}

   :prefer-metric-question
   {:description "Text when asking if user prefers metric units"
    :english     "Do you prefer metric units?"}

   :cycles-regular-question
   {:description "Text when asking if the user's cycles are regular"
    :english     "Are your cycles regular?"}

   :cycle-length-question
   {:description "Text when asking how many days are in a typical regular cycle"
    :english     "Typical Cycle Length?"}

   :weight-question
   {:description "Text when asking the user's weight"
    :english     "Weight?"}

   :height-question
   {:description "Text when asking the user's height"
    :english     "Height?"}

   :how-long-trying-to-conceive-question
   {:description "Text when asking how long the user has been trying to conceive"
    :english     "How long have you been trying to conceive?"}

   :conception-attempt-time-period-options
   {:description "The time period options for which a user may have been trying to conceive"
    :english     {0 "Not Trying"
                  1 "0-3 Months"
                  2 "3-6 Months"
                  3 "6-9 Months"
                  4 "9-12 Months"
                  5 "1-2 Years"
                  6 "2+ Years"}}

   :units
   {:description "Units of measurement for user profile info"
    :english     {:days   "days"
                  :lbs    "lbs"
                  :kgs    "kgs"
                  :feet   "ft"
                  :inches "in"
                  :cms    "cms"}}

   :event-map
   {:description "The map of available event types and their labels."
    :english     {:custom   "Custom Note"
                  :lh-low   "LH Test was Low"
                  :lh-high  "LH Test was High"
                  :pregnant "Found out I'm Pregnant"}}

   :event-time-selector-label
   {:description "Event time selector label"
    :english     (fn [t formats]
                   (let [{:keys [hour-of-day day-and-hour]} formats]
                     (if (.isSame (moment.) t "day")
                       (str "Today around " (-> (moment. t) (.format hour-of-day)))
                       (-> (moment. t) (.format day-and-hour)))))}
   })

(def ^:private terms-of-use
  "App terms of use
  Important: use three newline characters to separate paragraphs, use one newline character to separate lines."
  {
   ;; These are Sidley's Terms of Use, last updated March 24th 2016, exported from Google drive as text.
   ;; If the Google drive document is formatted such that a new paragraph gets an extra line of whitespace,
   ;; while a new topic within a new paragraph gets a newline, then the corresponding export
   ;; (saved in this project as doc/terms_of_use.txt) will have \n\n\n for new paragraphs and \n for new lines.
   ;; This paragraph/newline structure is relied upon for text formatting in the app,
   ;; so any changes should be accounted for.
   :terms-of-use
   {:description "The Terms of Use for Prima-Temp products.  User must accept these terms to create an account."
    :english "Terms and Conditions of Use\n(Last updated March 24, 2016)\nBy purchasing, accessing or using the Products and Services, you agree to be bound by the following terms and conditions for your purchase, access and use of the products and related services offered or provided by Prima-Temp, Inc. If you disagree with any part of these terms and conditions, then you should not purchase, access or use the Products and Services (as defined below) or any portion of the Products and Services. You agree that you are buying such products and services for your own personal use, and not for resale. \nIMPORTANT NOTICE: THESE TERMS AND CONDITIONS OF USE ARE SUBJECT TO BINDING ARBITRATION AND A WAIVER OF CLASS ACTION RIGHTS AS DETAILED BELOW UNDER THE HEADING “BINDING ARBITRATION AND CLASS ACTION WAIVER.”\n\n\nAcceptance of Terms\nPlease read these Terms and Conditions of Use (the “Terms”) carefully before purchasing or using any products or services offered or provided by Prima-Temp, Inc. (each, a “Product” or “Service” and, collectively, the “Products and Services”). Products include, without limitation, our proprietary Priya Ring product (the “Ring”) and any other products offered or provided by Prima-Temp. Services include, without limitation, our proprietary client device software application and its related service (the “Application”), our online website available at www.priyaring.com (the “Website”) and the provision of your access to any other applications, websites, online services, blogs, media pages or other services provided by or on behalf of Prima-Temp. \nThese Terms are between the purchaser (“you” or “your”) of the Products and Services and Prima-Temp, Inc. (“Prima-Temp”, “us”, “we”, or “our”) (“you and “us”, each a “Party” and collectively, the “Parties”) and govern your purchase and use of or access to the Products and Services. These Terms (i) include, and incorporate by reference, the Privacy Policy available at priyaring.com/privacy-policy (the “Privacy Policy”) and any other terms of use or privacy policies presented on the Application, the Website or any other Services operated by or on behalf of Prima-Temp, and (ii) constitute the entire agreement between you and Prima-Temp with respect to, and supersede any previous oral or written communications or documents (including, if you are obtaining an update, or any agreement that may have been included with an earlier version of the Products and Services) concerning the subject matter of the Terms. In no event will any additional or inconsistent term in any purchase order or similar document submitted by you modify these Terms. You further agree that your use of the Application shall be subject to and governed by any terms of usage set forth in the App Store from which you downloaded the Application, provided that such terms are consistent with these Terms. These Terms apply to all visitors, users, and others who access or use the Products and Services. \n\n\nChanges to Terms\nWe reserve the right, at our sole discretion, to modify or replace these Terms on a go forward basis at any time. We will post the updated Terms through the Products and Services or notify you of the updated Terms by email or other means. The updated Terms will be effective once they are posted on the Products and Services or provided to you. New or modified terms will not apply retroactively. Your continued use of the Products and Services following a change in the Terms represents your consent to the new Terms to the fullest extent permitted by law. We encourage you to periodically review these Terms. \nIf you do not agree to the new terms, please stop using the Products and Services.\n\n\nEligibility \nYou are eligible to purchase, use or access the Products and all of the Services only if you: (a) are at least 18 years old and have a customer address within the United States, (b) have internet service and an internet email address, (c) register your Ring and create your user account (“Account”) on the Website (requires computer with internet connection) (d) have a wireless device with internet access, and (e) consent to these Terms as provided herein. You must purchase a Ring in order to be eligible to access and use some of the Services. You are eligible to use portions of the Website without purchase or fee, provided you are at least 18 years old and agree to comply with these Terms.   \n\n\nOrders\nYou can place orders for Products and Services through the Website. All orders for Products and Services are subject to acceptance by us. Orders will be accepted by us “in writing” either by direct action on the Website or by confirmed receipted email (“Acceptance”). Orders not so accepted shall be deemed rejected. Orders for Products are subject to availability, and we reserve the right to cancel your order in our sole discretion. \n\n\nPrices\nThe prices payable for Products and Services shall be as displayed on the Website or otherwise agreed in writing by you and us at the time of purchase. We are not responsible for pricing, typographical or other errors in offers for Products and Services. Prices do not include charges for shipping and handling, for sales, use, or VAT taxes, or for other government required fees, such as for recycling, and any such taxes or fees are additional. We will charge you for such taxes and fees for orders shipped to states in which we are obligated to collect and report them. We reserve the right to change prices for Products at any time. \n\n\nTerms of Payment\nUnless otherwise stated by us in writing, you must pay us for Products and Services in advance. Our billing and payment process is managed by a third party service provider directly on our behalf. We or such provider may collect billing and payment information, such as credit card numbers and other information about you (e.g., address and zip code) that is necessary to ensure that the transaction is properly authorized.  Our service provider may retain such information to facilitate future transactions. \n\n\nDelivery and Title  \nAll times or dates given for delivery of Products are given in good faith and are not guaranteed. You agree that the date and time of delivery are not of the essence and that we shall not be liable to you for any delay in, or failure of, delivery. We will arrange for shipment on terms of F.O.B. shipping location, and you will be charged shipping and handling charges. Title (except to the extent that the Products consist of software) and risk of loss or damage to Products shipped shall pass to you upon delivery of the Products to the carrier.\n\n\nDamage and Non-Delivery Claims\nProducts potentially can be damaged during delivery or not-delivered. All claims for loss due to damage in transit or non-delivery must be made by you to Prima-Temp in writing: (a) for damage, within seven (7) days of the actual delivery date, or (b) for non-delivery, within ten (10) days of the scheduled delivery date as indicated in the Acceptance. You waive your right for a refund or to receive a replacement Product for damage during delivery or for non-delivery if you fail to submit a claim within the foregoing time periods. Call (833)-386-5222 or email support@prima-temp.com to obtain an RMA (return merchandise authorization) to arrange for the return of a damaged Product. All Product returns under this Section must be made in the original packaging with a valid sales receipt showing the date of purchase. Lost or delayed orders as a result of an address error submitted by you are not the responsibility of Prima-Temp. Orders that are returned to us as the result of an address error can be re-shipped at your expense.\n\n\nProduct Returns\nDue to the nature of the Products, all sales are final and non-refundable (except to the extent prohibited by applicable law), except that you have the right to return defective Products to us for replacement or a refund under the terms of the Limited Product Warranty described below.    \n\n\nLicense to Use Application and to Access Website and other Services\nThe Services (including the Application and any other software included in the Services) are licensed, not sold, to you under these Terms. Subject to these Terms including, without limitation, the above eligibility requirements, and contingent on your payment in full of the relevant fee for any fee based Services, Prima-Temp grants you a non-transferable, non-exclusive, non-assignable, revocable, limited license (without right of sublicense) to use and access the Services, the content provided or made available through the Services by or on behalf of Prima-Temp (including, without limitation, text, graphics, legends, customized graphics, original photographs, data, images, typefaces, titles, button icons, logos, designs, words or phrases, page headers, and software as well as the design, coordination, arrangement, enhancement  and presentation of this material, but excluding User Generated Content) (the “Prima-Temp Content”) and related documentation provided or made available to you by or on behalf of Prima-Temp (“Documentation”) for your personal use. This license includes the right to (i) install, access and use one (1) copy of the Application on your wireless device in machine executable object code form, (ii) the right to use the software that is embedded in any Products, and (iii) the right to access and use the Website. The license and rights granted herein do not grant you any right to any enhancement or update to, or support or telephone assistance with respect to, the Services. You agree and understand that the Services may be modified by Prima-Temp, in its sole discretion, at any time without prior notice. Any enhancements or modifications to the Services or the materials available on or through the Services including, without limitation, the Application and Website, as well as any new features or materials added to the Application or Website after your initial access to the them, shall be subject to these Terms (unless any such modified or new feature or material is accompanied by a separate agreement). \n\n\nAcceptable and Prohibited Use \nThe Products and Services may only be used in accordance with these Terms and the relevant User Manuals. The Products and Services, including materials and information contained in or generated by them, are intended for individual, personal use only and may not be used, sold, reproduced, distributed or in any way disseminated by persons not affiliated with Prima-Temp. You are authorized to use the Products and Services for personal, non-commercial use only. You may not use the Products and Services for any commercial purpose without the express, prior written consent of Prima-Temp.\nWe explicitly warn against and prohibit use of the Products and Services as a contraception tool. Reasonable effort has been made to ensure the accuracy of data and calculations; however, we accept no responsibility for reliance on this data or calculations. Certain information provided through the Products and Services may involve discussion of reproductive anatomy and/or sexual acts and, therefore, may not be appropriate for all users. If you are offended by material of a sexual nature, including information about conceiving, or discussion of reproductive anatomy, then you should not use the Products and Services.\nYou agree to use the Products and Services only for lawful purposes. You agree not to interrupt or attempt to interrupt the operation of the Products and Services in any way or use them in a way that, in our opinion, restricts, inhibits or interferes with the ability of any other user to enjoy them or that infringes on the privacy of any other user of the Products and Services, including by means of hacking or defacing any portion of the Products and Services, or by engaging in spamming, flooding, or other disruptive activities.\nYou may connect to the Services solely using a device that is manufactured, sold or distributed by or on behalf of Prima-Temp, the Application (as installed on a wireless device), the Website or any third-party application approved in writing by Prima-Temp. Any violation or attempted violation of this requirement may result in termination of your access to the Services.\n\n\nUnited States Only Restriction\nThe Products and Services are intended for use only by residents of the United States.  The Products and Services may not comply with legal requirements of foreign countries. Other countries may have laws or regulatory requirements that differ from those in the U.S. Any portion of the Products or Services is void where and to the extent prohibited by applicable law. \n\n\nMinor Children\nProviding personal information from children under the age of 18 through the Products and Services, including through interactive features, is prohibited.\n\n\nIntellectual Property \nPrima-Temp shall retain all applicable Intellectual Property Rights in and to the Products and Services, the Prima-Temp Content and the Documentation. You shall not acquire any rights in any of the foregoing other than those expressly specified in these Terms. You acknowledge that the Products and Services, Prima-Temp Content and Documentation contain proprietary information and trade secrets of Prima-Temp or its licensors, whether or not any portion thereof is or may be the subject of a valid copyright or patent. The term “Intellectual Property Rights” means all intellectual property rights throughout the world, whether existing under intellectual property, unfair competition or trade secret laws, or under statute or at common law or equity, including but not limited to: (i) copyrights, trademarks, trade secrets, trade names, patents, inventions, designs, logos and trade dress, “moral rights,” mask works, rights of personality, publicity or privacy, and any other intellectual property and proprietary rights; (ii) any registration, application or right to apply for any of the rights referred to in this clause; and (iii) any and all renewals, extensions and restorations thereof, now or hereafter in force.\nYou acknowledge and agree that the Prima-Temp Content is protected by copyrights, trademarks, service marks, patents or other proprietary rights and laws, and is the sole property of Prima-Temp or third party content providers. You are only permitted to use the Prima-Temp Content as expressly authorized by the specific content provider or us. You may not copy, reproduce, modify, republish, upload, post, scrape, transmit, translate, sell, offer for sale, create derivative worked based on or distribute any documents or information from the Services in any form or by any means without prior written permission from us or the specific content provider, and you are solely responsible for obtaining permission before reusing any copyrighted material that is available on though the Services. You may not decompile, disassemble, reverse engineer or otherwise attempt to derive, reconstruct, identify or discover any source code, underlying ideas, underlying user interface techniques or algorithms, of the Products and Services by any means; remove any proprietary notices, labels or marks from the Products and Services; or knowingly take any action that would cause the Products and Services to be placed in the public domain.  Any unauthorized use of the materials appearing or provided on the Services may violate copyright, trademark and other applicable laws and could result in criminal or civil penalties.\n\n\nLimited Product Warranty\nWe warrant to you that, if used and maintained in accordance with the relevant Product user manual (“User Manual”), the Product will be free from defects in materials and workmanship under normal use for a period of twenty (20) days (“Warranty Period”) from the date that the Product was originally purchased by you from us (the “Limited Product Warranty”). This Limited Product Warranty is personal to You and not transferable. No employee, agent or representative of Prima-Temp is authorized to make any warranties or representations on behalf of Prima-Temp or to modify this Limited Product Warranty. \nPrima-Temp’s obligation under this Limited Product Warranty shall be limited to, at Prima-Temp’s sole option and without charge to you, replacing with a new Product, or providing a refund of the purchase price you paid for, any Product that we determine to be defective, either in material or workmanship, under normal use within the Warranty Period, when the Product is returned to Prima-Temp during or within three (3) days after expiration of the Warranty Period. We are responsible for the cost of shipping the defective Product to us and the cost of shipping the replacement Product to You.  Any refunded or replaced Products shall become the property of Prima-Temp.\nThis Limited Product Warranty Does NOT Cover:\n* Damage caused by improper use or failure to follow Prima-Temp’s instructions.\n* Cosmetic damage, including scratches, dents, cracks and similar damage.\n* Damage caused by use with products or services not sold or provided by Prima-Temp for use with the Product.  \n* Damage caused intentionally or by modifications or alterations to the Product not authorized by Prima-Temp; damage caused by acts outside of Prima-Temp’s control including, without limitation, accident, misuse, abuse, tampering, negligence or wilful misconduct, exposure to weather or water, improper repair, operating a Product outside its permitted or intended uses as defined in the User Manual or other documentation provided or made available to you.\n* Software (including, without limitation, the Application or any software embedded in a Product).\n* Services\n* Normal wear and tear.\n* Corruption or loss of data or any media present on any hard drive or storage device\nTHE DURATION OF ANY IMPLIED WARRANTY, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE, IS LIMITED TO THE DURATION OF THE WARRANTY PERIOD. THE REPLACEMENT OR REFUND PROVIDED UNDER THIS LIMITED PRODUCT WARRANTY IS YOUR SOLE AND EXCLUSIVE REMEDY, AND PRIMA-TEMP’S SOLE AND EXCLUSIVE LIABILITY UNDER, THIS LIMITED PRODUCT WARRANTY, AND IS PROVIDED IN LIEU OF ALL OTHER WARRANTIES AND REMEDIES, EXPRESS AND IMPLIED. This Limited Product Warranty gives you specific rights and you may also have other rights which may vary from state to state. Some states do not allow a limitation on how long an implied warranty lasts so this limitation and exclusion may not apply to you.\nIf you believe the Product is defective or malfunctioning, please first utilize the resources available in your User Manual for troubleshooting. \nCall (888) 866-1032 or email support@prima-temp.com to make a claim for and to obtain an RMA (return merchandise authorization) to arrange for the return of the Product to us so that we can confirm the defect (“Confirmed Defect”). All Product returns must be made in the original packaging with a valid sales receipt showing the date of purchase. We will examine the returned Product to substantiate your claim and will use commercially reasonable efforts to notify you of any Confirmed Defect within thirty (30) calendar days from the date we receive the returned Product from you. In the event of a Confirmed Defect, we will provide you with a replacement Product or a refund, at our option, within a reasonable time period.\n\n\nDisclaimer of Warranty – Product and Services  \nEXCEPT AS EXPRESSLY SET FORTH IN THESE TERMS, (I) THE PRODUCTS AND SERVICES, PRIMA-TEMP CONTENT AND DOCUMENTATION ARE PROVIDED “AS IS,” “AS AVAILABLE” AND “WITH ALL FAULTS”, AND THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE PRODUCTS AND SERVICES, PRIMA-TEMP CONTENT AND DOCUMENTATION IS WITH YOU, AND (II) ALL WARRANTIES WITH RESPECT TO THE PRODUCTS AND SERVICES, PRIMA-TEMP CONTENT AND DOCUMENTATION, EXPRESS OR IMPLIED, INCLUDING ANY WARRANTIES OF NON-INFRINGEMENT OR LACK OF VIRUSES OR ERRORS ARE HEREBY DISCLAIMED. NO INFORMATION CONVEYED BY THE PRODUCTS AND SERVICES EITHER ORALLY OR IN WRITING SHALL CREATE SUCH A WARRANTY. THERE IS NO GUARANTEE THAT USE OF THE PRODUCTS AND SERVICES WILL ASSIST A USER IN GETTING PREGNANT OR NOT GETTING PREGNANT. PRIMA-TEMP ACCEPTS NO RESPONSIBILITY FOR THE INFORMATION OR ADVICE POSTED IN ANY PART OF THE SERVICES INCLUDING, WITHOUT LIMITATION, USER GENERATED CONTENT OR ANY OTHER INFORMATION OR ADVICE INCLUDED OR POSTED ON THE APPLICATION, THE WEBSITE OR ON ANY FORUM, BLOG, APPLICATION.\nExcept as expressly set forth in these Terms, Prima-Temp and its suppliers make no guarantees and disclaim all warranties and representations about the Products and Services’ accuracy, relevance, timeliness or completeness, and do not warrant that the Products and Services will meet your requirements in any respect, operate securely or be available at all times, or that the operation of the Products and Services will be uninterrupted or error-free, or that defects or errors in the Products and Services or nonconformity to its Documentation can or will be corrected. \nWe do not represent or warrant that any version of the Products and Services, or any portion thereof, will be compatible with any hardware or software versions or applications (including any future versions or updates of your phone, tablet, computer or its operating system) or provide the same functionality that is provided by the current version of the Products and Services. The Products and Services may not be compatible with your hardware or software versions or applications (including any specific versions of your phone, tablet, computer, or its specific operating system). Prima-Temp does not undertake any obligation to provide the Products and Services to you in a way that is compatible with your hardware or software.\n\n\nLimitation of Liability  \nTO THE FULLEST EXTENT PERMITTED BY LAW, IN NO EVENT AND REGARDLESS OF THE FORM OF ACTION WILL PRIMA-TEMP OR ITS SUPPLIERS OR LICENSORS BE LIABLE FOR ANY INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES WHATSOEVER ARISING OUT OF THESE TERMS, YOUR USE OF OR INABILITY TO USE THE PRODUCTS AND SERVICES, YOUR ACCESS TO OR YOUR INABILITY TO ACCESS THE PRODUCTS AND SERVICES, THE INACCURACY OR LOSS OF ANY DATA GENERATED OR MADE AVAILABLE BY THE PRODUCTS AND SERVICES OR YOUR RELIANCE ON ANY SUCH DATA, OR FOR DAMAGES IN AN AMOUNT GREATER THAN THE ACTUAL PURCHASE PRICE PAID BY YOU FOR THE PRODUCT AND SERVICES GIVING RISE TO THE DAMAGES, EVEN IF PRIMA-TEMP HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.\nSome states do not allow the exclusion of certain warranties or the limitation or exclusion of liability for incidental or consequential damages.  Accordingly, some of the above limitations and disclaimers may not apply to you.  To the extent that Prima-Temp may not, as a matter of applicable law, disclaim any implied warranty or limit its liabilities, the scope and duration of such warranty and the extent of Prima-Temp’s liability will be equal to the amount the user paid for the relevant portion of the Products and Services or the minimum period permitted under such applicable law.\nYou acknowledge and agree that Prima-Temp has offered the Products and Services, set their  prices, and entered into these Terms in reliance upon the warranty disclaimers and the limitations of liability set forth herein, that the warranty disclaimers and the limitations of liability set forth herein reflect a reasonable and fair allocation of risk between you and Prima-Temp, and that the warranty disclaimers and the limitations of liability set forth herein form an essential basis of the bargain between you and Prima-Temp. You acknowledge and agree that Prima-Temp would not be able to provide the Products and Services on an economically reasonable basis without these limitations.\n\n\nNot Medical Advice\nThe Products and Services are intended to help you measure and record your core body temperatures, and are NOT intended for use in the diagnosis of disease or other conditions, including a determination of the state of health, in order to cure, mitigate, treat, or prevent disease or its sequelae. The Products and Services contain general information relating to health topics, specifically core body temperature, and are for educational purposes only, but they are no substitute for medical judgement, advice, diagnosis or treatment of any health condition or problem. The Products and Services do not provide comprehensive information concerning any particular disease or medical condition. You should not rely on information provided by the Products or Services to diagnose or treat health problems.  Use of the Products and Services does not create an express or implied physician-patient relationship.\nProper treatment of health conditions depends upon a number of factors, including among other things, your medical history, diet, lifestyle, and medication regimen. Your health care provider can best assess and address your individual health care needs. You should not make healthcare decisions, undertake any medical actions or not undertake any medical actions, or avoid or delay obtaining medical advice from a licensed health care practitioner because of something that was read on the Products and Services.  \nTHE INFORMATION PROVIDED ON OR THROUGH THE PRODUCTS AND SERVICES DOES NOT CONSTITUTE MEDICAL ADVICE, NOR IS IT INTENDED TO REPLACE THE NECESSITY OF CONSULTATION WITH A PHYSICIAN. NO REPRESENTATION IS MADE BY PRIMA-TEMP REGARDING THE ACCURACY OR IMPORTANCE OF ANY INFORMATION REFERENCED HEREIN. YOU SHOULD ALWAYS CONSULT AN APPROPRIATE HEALTH CARE PROFESSIONAL FOR SPECIFIC ADVICE TAILORED TO YOUR SITUATION. THE PRODUCTS AND  SERVICES ARE NOT INTENDED TO DIAGNOSE, TREAT, CURE, OR PREVENT ANY DISEASE.\n\n\nLinks to Third Party Web Sites\nThe Services may display, include, or make available content, data, information, applications or materials from third parties (“Third Party Materials”) or provide links to certain third party websites or services that are not owned or controlled by Prima-Temp.\nPrima-Temp has no control over, and assumes no responsibility or liability for, the availability, content, privacy policies, practices, or performance of the website of, or the services or materials provided by, any third party listed, described or discussed in the Services. Prima-Temp is providing these Third Party Materials and links to you only as a convenience, and the inclusion of any Third Party Materials or links does not imply endorsement by Prima-Temp of such third party, or any goods or services available through the third party, or any association with the third party’s operators.\nWe strongly advise you to read the terms and conditions and privacy policies of any third-party web sites or services that you visit.\n\n\nDMCA Notice\nIf you believe that your Intellectual Property Rights have been violated by something on our Services, please contact our Copyright Agent Santangelo Law Offices (Luke Santangelo) at 125 South Howes, 3rd Floor, Fort Collins, CO 80521 and provide the following information:\n* A physical or electronic signature of a person authorized to act on behalf of the owner of an exclusive right that is allegedly infringed;\n* Identification of or a representative list of the work you believe has been infringed;\n* Identification of the material that is claimed to be infringing or to be the subject of infringing activity and that is to be removed or access to which is to be disabled, and information reasonably sufficient to permit Prima-Temp to locate the material;\n* Information reasonably sufficient to permit Prima-Temp to contact you;\n* A statement that you have a good faith belief that use of the material in the manner complained of is not authorized by the copyright owner, its agent, or the law.\n* A statement that the information in the notification is accurate, and under penalty of perjury, that you are authorized to act on behalf of the owner of an exclusive right that is allegedly infringed.\n\n\nPrivacy\nYour Privacy is important to Prima-Temp. Any personal information that you transmit to the Services or to us, whether by electronic mail, postal mail, post/blog, or other means, for any reason are subject to our Privacy Policy. These Terms include and incorporate the Prima-Temp Privacy Policy, available at priyaring.com/privacy-policy, which is integrated herein by this reference. Please read the Privacy Policy carefully for details relating to the collection, use, and disclosure of personal information. \n\n\nSecurity and Password\nYou are solely responsible for maintaining the confidentiality of your password and Account and for any and all statements made and acts or omissions that occur through the use of your password and Account. Therefore, you must take steps to ensure that others do not gain access to your password and account. Our personnel will never ask you for your password. You may not transfer or share your Account with anyone, and we reserve the right to immediately terminate your Account if you do transfer or share your Account.\n\n\nCommunity Interactive Features  and User Generated Content\nYou and other users may post comments, ideas, suggestions, information, files, images or other materials (“User Generated Content”) on the Services. You retain all rights to the User Generated Content that you post. By making your User Generated Content available on or through the Services you grant to Prima-Temp a non-exclusive, transferable, sublicensable, world-wide, royalty-free license to use, copy, modify, publicly display, publicly perform and distribute your User Generated Content in connection with operating and providing the Services.\nYou understand that all User Generated Content posted to our Services is the sole responsibility of the individual who originally posted the content. You understand, also, that all opinions expressed by users of the Services are expressed strictly in their individual capacities, and not as our representatives or any of our sponsors or partners. The opinions that you or others post on our Services do not necessarily reflect our opinions.\nYou warrant and represent that you either own or otherwise control all of the rights to the User Generated Content that you post, including, without limitation, all the rights necessary for you to provide, post, upload, input or submit the User Generated Content, or that your use of such content is a protected fair use. You agree that you will not knowingly provide material and misleading false information. You represent and warrant that the User Generated Content you supply does not violate these Terms. It is your sole responsibility to ensure that your postings do not disclose confidential and/or proprietary information, including personal health information, information covered by a nondisclosure agreement, and information that you are not otherwise authorized to disclose. We caution you not to disclose personal information about yourself or your children, as the community features are public.\nYou are strictly prohibited from posting or communicating on or through the Services any unlawful, harmful, offensive, threatening, abusive, libelous, harassing, defamatory, vulgar, obscene, profane, hateful, fraudulent, sexually explicit, homophobic, racially, ethnically or otherwise objectionable content or material of any sort, including, but not limited to, any material that encourages conduct that would constitute a criminal offense, gives rise to civil liability or otherwise violates any applicable local, state, national, or international law.\nYou agree to indemnify and hold us and our affiliated companies, and their directors, officers and employees, harmless for any and all claims or demands, including reasonable attorney fees, that arise from or otherwise relate to your use of the interactive features, any User Generated Content you supply to the Services, or your violation of these Terms or the rights of another. \nYou agree not to contact other users of the Services through unsolicited e-mail, telephone calls, mailings or any other method of communication.\n\n\nIndemnification\nYou agree to indemnify, defend and hold harmless Prima-Temp, its officers, directors, employees, agents, suppliers and third party partners from and against all losses, expenses, damages and costs, including reasonable attorneys' fees, from your use of the Products and Services or resulting from any violation by you of these Terms, to the fullest extent allowed by law. \n\n\nBinding Arbitration and Class Action Waiver\nPLEASE READ THE FOLLOWING CAREFULLY AS IT MAY SIGNIFICANTLY AFFECT YOUR LEGAL RIGHTS, INCLUDING YOUR RIGHT TO FILE A LAWSUIT IN A COURT OF LAW\nInitial Dispute Resolution\nContact us at any time by email at support@prima-temp.com and we will be happy to address any dispute, claim or disagreement  you may have regarding the Products and Services. The Parties will use reasonable efforts to settle any such dispute, claim or disagreement, and good faith negotiations to reach a settlement shall be a condition to either Party initiating a lawsuit or arbitration.\nBinding Arbitration\nIf the Parties do not agree to a settlement within 90 days from the time you notify us of the dispute, claim or disagreement under the above paragraph, either Party may then initiate binding arbitration as the sole means to resolve claims, subject to the terms set forth herein. Specifically, all claims arising out of or relating to these Terms (including their formation, performance, and breach), the Parties’ relationship with each other and/or your use of the Products and Services shall be finally settled by binding arbitration administered by the American Arbitration Association (“AAA”), excluding any rules or procedures governing or permitting class actions. \nThe arbitrator, not any federal, state, or local court or agency, shall have exclusive authority to resolve all disputes arising out of or relating to the interpretation, applicability, enforceability, or formation of these Terms, including but not limited to any claim that all or any part of these Terms are void or voidable, or whether a claim is subject to arbitration. The arbitrator shall be empowered to grant whatever relief would be available in a court under law or in equity. The arbitrator’s aware shall be written, and binding on the Parties and may be entered as a judgment in any court of competent jurisdiction.\nLocation\nArbitration will take place at any reasonable location within the United States convenient for you. \nClass Action Waiver\nThe Parties further agree that any arbitration shall be conducted only in their individual capacities and not as a class action or other representative action, and the Parties expressly waive their right to file a class action or seek relief on a class basis. YOU AND PRIMA-TEMP AGREE THAT EACH PARTY MAY BRING CLAIMS AGAINST THE OTHER ONLY IN YOUR OR ITS INDIVIDUAL CAPACITY, AND NOT AS A PLAINTIFF OR CLASS MEMBER IN ANY PURPORTED CLASS OR REPRESENTATIVE PROCEEDING. If any court or arbitrator determines that the class action waiver set forth in this paragraph is void or unenforceable for any reason or that an arbitration can proceed on a class basis, then the arbitration provision set forth above shall be deemed null and void in its entirety and the Parties shall be deemed to have not agreed to arbitrate disputes.\nException – Litigation of Intellectual Property and Small Claims Court Claims\nNotwithstanding the Parties' decision to resolve all disputes through arbitration, either Party may bring an action in state or federal court to protect its Intellectual Property Rights. Either Party may also seek relief in a small claims court for disputes or claims within the scope of that court’s jurisdiction.\n30-Day Right to Opt Out\nYou have the right to opt-out and not be bound by the arbitration and class action waiver provisions set forth above by sending written notice of your decision to opt-out to the following address: 2820 Wilderness Place, Suite C, Boulder, CO 80301 or support@prima-temp.com. The notice must be sent within 30 days of your first use of the Services. If you do not opt-out within 30 days, you shall be bound to arbitrate disputes in accordance with the terms of those paragraphs. If you opt-out of these arbitration provisions, Prima-Temp will not be bound by them.\nChanges to this Section\nPrima-Temp will provide at least 60-days’ notice of any changes to this section. Changes will become effective on the 60th day, and will apply prospectively only to any claims arising after the 60th day. \nFor any dispute not subject to arbitration, you and Prima-Temp agree to submit to the personal and exclusive jurisdiction of and venue in the federal and state courts located in California. You further agree to accept service of process by mail, and hereby waive any and all jurisdictional and venue defences otherwise available. \n\n\nTERMINATION OF ACCESS/REMOVAL OF CONTENT \nTHESE TERMS ARE EFFECTIVE UNTIL TERMINATED. YOU MAY TERMINATE THESE TERMS AT ANY TIME BY DESTROYING ALL COPIES OF THE APPLICATION APPLICABLE TO THE SERVICES AND RELATED DOCUMENTATION UNDER YOUR CONTROL, AND DELETING YOUR ONLINE ACCOUNT. ALL LICENSE RIGHTS GRANTED TO YOU UNDER THESE TERMS WILL IMMEDIATELY TERMINATE IF YOU VIOLATE ANY OF THESE TERMS.\nWE MAY TERMINATE OR SUSPEND YOUR ACCESS TO THE SERVICES IMMEDIATELY, WITHOUT PRIOR NOTICE OR LIABILITY, FOR ANY REASON WHATSOEVER INCLUDING, WITHOUT LIMITATION, IF (I) YOU BREACH ANY PROVISION OF THESE TERMS, (II) PRIMA-TEMP IS REQUIRED TO DO SO BY APPLICABLE LAW, (III) PRIMA-TEMP DECIDES TO NO LONGER PROVIDE ALL OR ANY PORTION OF THE SERVICES TO YOU OR GENERALLY. WE SHALL HAVE THE RIGHT IN OUR SOLE DISCRETION TO SUSPEND OR TERMINATE YOUR ACCESS TO AND USE OF THE SERVICES AND/OR REMOVE ANY OF YOUR CONTENT CONTAINED ON THE SERVICES SHOULD WE CONSIDER YOUR STATEMENTS OR CONDUCT OR USE OF THE SERVICES TO BE INACCURATE, ILLEGAL, OBSCENE, DEFAMATORY, THREATENING, INFRINGING OF INTELLECTUAL PROPERTY RIGHTS, INVASIVE OF PRIVACY, INJURIOUS, OBJECTIONABLE, OR OTHERWISE IN VIOLATION OF THESE TERMS OR APPLICABLE LAW. ALL PROVISIONS OF THE TERMS, WHICH BY THEIR NATURE SHOULD SURVIVE TERMINATION, SHALL SURVIVE TERMINATION, INCLUDING, WITHOUT LIMITATION, OWNERSHIP PROVISIONS, WARRANTY DISCLAIMERS, INDEMNITY AND LIMITATIONS OF LIABILITY.\n\n\nSeverability\nIf any provision of these Terms are found to be invalid or unenforceable, they will be enforced to the extent permissible and, to the extent invalid or unenforceable, such provision shall be deemed modified to the most limited extent necessary to be valid and enforceable, in accordance with applicable law, while still as fully as possible carrying out the intent of the original provision, and the remainder of these Terms will remain in full force and effect.  \n\n\nWaiver\nFailure by Prima-Temp to enforce any right or provision of these Terms or otherwise prosecute any right with respect to a default hereunder will not constitute a waiver by Prima-Temp of the right to enforce rights with respect to the same or any other breach.\n\n\nGoverning Law\nThese Terms shall be governed and construed in accordance with the laws of the State of California, as such laws apply to contracts between California residents entered into and performed entirely in California, notwithstanding your place of residency or California’s conflicts of law provisions. \n\n\nContact Us\nIf you have any questions about these Terms, please contact us at:"}
   })

(def ^:private verbiage
  "All internationalized verbiage"
  (merge text-constants emails defaults date-time-formats notifications composite-alerts alerts confirmation-dialogs text terms-of-use))

(defn get-text
  "Returns internationalized text."
  [text-identifier & args]
  (let [current-language :english
        value (get-in verbiage [text-identifier current-language])]
    (when (nil? value) (println "ERROR: No translation in" (name current-language) "for " text-identifier))
    (if (fn? value) (apply value args) value)))






