## First version

With this project being my first time working with Kotlin, I wanted to go with a simple first version without considering the scheduling requirement yet; that would be tackled later on.

Of course, this was also my first experience with MockK. Coming from Mockito, I found some similarities, but also a few small differences; for instance, `when` function to express what is returned from a mock object is replaced with every, which IMO reduces confusion as this is usually defined in the `given` phase of the test, not in the `when` one.

## Second version

Updated the status of the paid invoices. Also, added some endpoint to be used for charging pending invoices (besides the scheduled charging), which also can help for functional testing purposes.

## Third version

At this point, I added the scheduled task for running the payment of the pending invoices. I have usually worked with the Spring @Scheduled annotation in these type of cases, using a cron expression for the scheduling, so another first time for me here!

I also included some Javadoc for the functions that I added.
