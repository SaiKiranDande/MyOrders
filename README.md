# MyOrders
In this App I have integrated the firebase Realtime database.
The Libraries I have used:
1 -> Last Adapter //This for recyclerview adapter and binding the data.
2 -> EventBus //This is for passing the data using events between activities or fragments.

This Application contains of 4 screens
Screen 1 -> 
Splash screen - with logo which displays for 2 seconds and if user is not loged in then lands on login screen or else MyOrders Screen.

Screen 2 ->
Login Sceen - Here user enters the email id and password then on click on login button checks for proper validations for email id and password. 
Password must be minimum 6 charecter length and if user checks on remember me then when ever user launches the app user is directly taken to MyOrders screen from splash screen.

Screen 3 -> 
MyOrders - Here it displays all the list of orders in a recylerview, and on scroll of recyclerview the fab button hides and on scroll to top again fab displays. 
The search functionality searches for order name and customer name. Logout functionality clears the user details and lands on Login screen. 
on Click of item delete option the particular item will be deleted from firebase using a unique order number, if user clicks on item edit it launches to update order details screen with data populated on views. 
On click of fab buttun user navigated to AddDetails screen

Screen 4 -> 
Add/UpdateOrderDetails - In this screen user adds the details and saves the data in firebase, If user is editing the details the order details will be updated in firebase. In this screen user has to enable location, it takes users current geo location. 
