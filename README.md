l# whatsApp

- Android client application for online messaging written in Java. 
- Backend glassfish server connected to mysql database for handeling users and messages.
- Communication between client and server using RESTful API and websockets.


#Todo
- Fix bug when starting conversation whitout messages.
- Finish push service.
- Save messages locally instead of downloading every time.
- Add a message broadcast in the pushserivce and a reviever in messageactivity to connect the service to the activity
- Remove the timer from messageactivity, and change onResume() to fetch new messages, only then and on send it is needed. 
- Implement remove notification when conversation is open
- Add emoji support
- Encrytp messages and passwords
- Implement DH key-exhange 

