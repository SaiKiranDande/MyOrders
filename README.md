## MyOrders

In this App, I have integrated the Firebase Realtime Database. 

The Libraries I have used: 

**LastAdapter**
  This for recyclerview adapter and binding the data. [Github pages](https://github.com/nitrico/LastAdapter)


**EventBus**
  This is for passing the data using events between activities or fragments. [Github pages](https://github.com/greenrobot/EventBus)

**Firebase**
From this I have integrated the Realtime Database.

This Application contains 4 screens.

**Screen 1 - Splash screen** 

1. with a logo which displays for 2 seconds and if a user is not logged in then lands on the login screen or else MyOrders Screen. (The unique authentication is not added)

**Screen 2 - Login Screen** 

1. Here user enters the email id and password then on click on login button checks for proper validations for email id and password. 
2. Password must be minimum 6 characters length and if user checks on remember me then whenever the user launches the app user is directly   taken to MyOrders screen from splash screen.

**Screen 3 - MyOrders**

1. Here it displays all the list of orders in a recycler view, and on the scroll of recycler view the fab button hides and on scroll to      top again fab displays. 
2. The search functionality searches for order name and customer name.
3. Logout functionality clears the user details and lands on the Login screen.
4. On Click of item delete option the particular item will be deleted from firebase using a unique order number, if the user clicks on item edit it launches to update order details screen with data populated on views. 
5. On click of fab button user navigated to AddDetails screen.

**Screen 4 - Add/UpdateOrderDetails** 

1. In this screen, the user adds the details and saves the data in firebase, If the user is editing the details the order details will be updated in firebase. 
2. Done all the validations for the fields, if any validation fails displays a toast message. 
3. In this screen user has to enable location, it takes users current geolocation. User can not change his current location.


**Note** : Internet and Location permissions are must for the application.
