# java_cardInfo

process - figma prototype(add link) -> frontend -> database -> backend

for databse, used firebase - because generally better for working with other people and security and out of 2 options - choose firestore database.
for example for mysql would need to configure docker as well and would only persist for local user.
could use in memory H2 database but that is comparatively more to set up than firebase

Explain why have 2 field - encryptedPAN and lastfour hashed, so would not have to provide key and hence more security
(because even if i bind it with like GCP or AWS would still need to give access to that)

TODO
MAKE SURE WITH ./gradlew the webpage gets opened on its own - done
search - done
security - done
improve frontend - done
improve readme 
add comments
make code more readable

figma link
https://www.figma.com/design/IUmM4cdMf6xA9HQIMw1ZAv/Card_management?node-id=0-1&t=1cB3sJ1r70qxiB0M-1



REMEBER
DO NOT PUSH THE JSON FILE WITH API KEY


endpoints
/api/cards
/api/cards/search
