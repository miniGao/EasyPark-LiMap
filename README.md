# EasyPark-LiMap and AutoCancellation

Individual Task for EasyPark Group Project

## Installation

```
android 7.1.1 and above
```

## Deliverable

```
1. Search page (Map Fragment)
1.1 Use Google Map API
1.2 Use Android Map Fragment
1.3 Retrieve all parking lot information from Firestore Database (location, price, parking spots with availability status)
1.4 Synchronize and update Map information with Firestore every 5 seconds.
1.5 Sync service will stop when application goes to background, and resume when user switches back to the application.
1.6 Auto track user’s location while user is moving (every 5 seconds)
1.7 Search filter for price (Real Time Search (able to react to price change in database within 5 seconds) through Firestore database for parking prices and indicate parking lot on Map (with Red marker) that has the lowest price for 1-hour parking, 6-hour parking, 12-hour parking, 24-hour parking)
1.8 Search filter for nearest distance (Real Time Search (able to react to location change of database and location change of user within 5 seconds) parking lot location is retrieved from Firestore Parking lot Table[GeoPoint])
1.9 React to User Selection of Parking Lot on Map, Pass ParkingLotId to the reservation Activity.

2. Auto cancellation of Reservation if user doesn’t arrive at Parking lot in time (10 seconds for demo purpose).
2.1 When a specific reservation is made, start a timer countdown using Android OS handler.
2.2 When indicated time runs out, the reservation will be deleted programmatically from the Firestore database.
2.3 When the user presses the “I Arrived” button within indicated duration representing user’s arrival at Parking Lot, timer stops and reservation is kept at Firestore database.
```

## Author
miniGao