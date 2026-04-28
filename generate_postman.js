const fs = require('fs');

const json = (body) => JSON.stringify(body, null, 2);

const authHeader = [{ key: 'Authorization', value: 'Bearer {{token}}' }];
const jsonHeader = [{ key: 'Content-Type', value: 'application/json' }];
const authedJsonHeader = [
  { key: 'Content-Type', value: 'application/json' },
  { key: 'Authorization', value: 'Bearer {{token}}' }
];

const collection = {
  info: {
    name: 'FlatMate App API - v7',
    description: 'Updated Postman collection aligned with current Spring controllers, including switch-role flow and current location/chat/notification endpoints.',
    schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json'
  },
  variable: [
    { key: 'baseUrl', value: 'https://hjqqy3z9r1.execute-api.ap-south-1.amazonaws.com/prod', description: 'Production API base URL. Replace with http://localhost:8081 for local testing.' },
    { key: 'token', value: '<paste accessToken here>' },
    { key: 'refreshToken', value: '<paste refreshToken here>' },
    { key: 'userId', value: '+919876543210' },
    { key: 'ownerId', value: '+919876543210' },
    { key: 'seekerId', value: '+919876543210' },
    { key: 'listingId', value: 'listing_001' },
    { key: 'matchId', value: 'match_001' },
    { key: 'kycDocumentUrl', value: 'https://s3.amazonaws.com/flatmate-photos/kyc/user_001/document/aadhar-front.jpg' },
    { key: 'kycSelfieUrl', value: 'https://s3.amazonaws.com/flatmate-photos/kyc/user_001/selfie/selfie.jpg' }
  ],
  item: [
    {
      name: 'Health',
      item: [
        {
          name: 'Get Health',
          request: {
            method: 'GET',
            url: { raw: '{{baseUrl}}/health', host: ['{{baseUrl}}'], path: ['health'] }
          }
        }
      ]
    },
    {
      name: 'Auth',
      item: [
        {
          name: '1. Send OTP',
          request: {
            method: 'POST',
            header: jsonHeader,
            url: { raw: '{{baseUrl}}/auth/otp/send', host: ['{{baseUrl}}'], path: ['auth', 'otp', 'send'] },
            body: { mode: 'raw', raw: json({ phone: '+919876543210' }) }
          }
        },
        {
          name: '2. Verify OTP',
          request: {
            method: 'POST',
            header: jsonHeader,
            url: { raw: '{{baseUrl}}/auth/otp/verify', host: ['{{baseUrl}}'], path: ['auth', 'otp', 'verify'] },
            body: { mode: 'raw', raw: json({ phone: '+919876543210', otp: '123456' }) },
            description: 'Returns accessToken, refreshToken, roles, activeRole and onboarding flags.'
          }
        },
        {
          name: '3. Refresh Token',
          request: {
            method: 'POST',
            header: jsonHeader,
            url: { raw: '{{baseUrl}}/auth/refresh', host: ['{{baseUrl}}'], path: ['auth', 'refresh'] },
            body: { mode: 'raw', raw: json({ refreshToken: '{{refreshToken}}' }) }
          }
        },
        {
          name: '4. Set Role',
          request: {
            method: 'POST',
            header: authedJsonHeader,
            url: { raw: '{{baseUrl}}/auth/set-role', host: ['{{baseUrl}}'], path: ['auth', 'set-role'] },
            body: { mode: 'raw', raw: json({ userId: '{{userId}}', role: 'SEEKER' }) },
            description: 'Accepted values: SEEKER, OWNER, TENANT, HOST. TENANT maps to SEEKER and HOST maps to OWNER.'
          }
        },
        {
          name: '5. Switch Role to Seeker',
          request: {
            method: 'POST',
            header: authedJsonHeader,
            url: { raw: '{{baseUrl}}/auth/switch-role', host: ['{{baseUrl}}'], path: ['auth', 'switch-role'] },
            body: { mode: 'raw', raw: json({ userId: '{{userId}}', role: 'SEEKER' }) }
          }
        },
        {
          name: '6. Switch Role to Owner',
          request: {
            method: 'POST',
            header: authedJsonHeader,
            url: { raw: '{{baseUrl}}/auth/switch-role', host: ['{{baseUrl}}'], path: ['auth', 'switch-role'] },
            body: { mode: 'raw', raw: json({ userId: '{{userId}}', role: 'OWNER' }) }
          }
        }
      ]
    },
    {
      name: 'Profiles',
      item: [
        {
          name: '1. Get Profile',
          request: {
            method: 'GET',
            header: authHeader,
            url: { raw: '{{baseUrl}}/profiles/{{userId}}', host: ['{{baseUrl}}'], path: ['profiles', '{{userId}}'] }
          }
        },
        {
          name: '2. Update Basic Profile',
          request: {
            method: 'POST',
            header: authedJsonHeader,
            url: { raw: '{{baseUrl}}/profiles', host: ['{{baseUrl}}'], path: ['profiles'] },
            body: {
              mode: 'raw',
              raw: json({
                userId: '{{userId}}',
                firstName: 'Rajasekar',
                lastName: 'Kumar',
                dateOfBirth: '1995-06-15',
                gender: 'MAN',
                bio: 'Software engineer looking for a flat near Koramangala.',
                profilePic: 'https://s3.amazonaws.com/flatmate-photos/users/user_001/profile.jpg'
              })
            }
          }
        },
        {
          name: '3. Update Seeker Profile',
          request: {
            method: 'POST',
            header: authedJsonHeader,
            url: { raw: '{{baseUrl}}/profiles', host: ['{{baseUrl}}'], path: ['profiles'] },
            body: {
              mode: 'raw',
              raw: json({
                userId: '{{userId}}',
                seekerProfile: {
                  education: "Bachelor's",
                  jobTitle: 'Software Engineer',
                  companyName: 'TCS',
                  knownLanguages: ['Tamil', 'English', 'Hindi'],
                  smokingHabit: 'NEVER',
                  drinkingHabit: 'SOMETIMES',
                  foodHabit: 'NON_VEGETARIAN',
                  maritalStatus: 'SINGLE',
                  petHabit: 'NO_PETS',
                  description: 'Looking for calm and clean flatmates.',
                  location: {
                    latitude: 12.9716,
                    longitude: 77.5946,
                    formattedAddress: 'Koramangala, Bangalore'
                  }
                }
              })
            }
          }
        },
        {
          name: '4. Get KYC Upload URL - Document',
          request: {
            method: 'GET',
            header: authHeader,
            url: {
              raw: '{{baseUrl}}/profiles/{{userId}}/kyc/upload-url?fileName=aadhar-front.jpg&type=document',
              host: ['{{baseUrl}}'],
              path: ['profiles', '{{userId}}', 'kyc', 'upload-url'],
              query: [
                { key: 'fileName', value: 'aadhar-front.jpg' },
                { key: 'type', value: 'document' }
              ]
            }
          }
        },
        {
          name: '5. Get KYC Upload URL - Selfie',
          request: {
            method: 'GET',
            header: authHeader,
            url: {
              raw: '{{baseUrl}}/profiles/{{userId}}/kyc/upload-url?fileName=selfie.jpg&type=selfie',
              host: ['{{baseUrl}}'],
              path: ['profiles', '{{userId}}', 'kyc', 'upload-url'],
              query: [
                { key: 'fileName', value: 'selfie.jpg' },
                { key: 'type', value: 'selfie' }
              ]
            }
          }
        },
        {
          name: '6. Complete KYC',
          request: {
            method: 'POST',
            header: authedJsonHeader,
            url: { raw: '{{baseUrl}}/profiles/kyc', host: ['{{baseUrl}}'], path: ['profiles', 'kyc'] },
            body: {
              mode: 'raw',
              raw: json({
                userId: '{{userId}}',
                documentType: 'AADHAR_CARD',
                documentImageUrl: '{{kycDocumentUrl}}',
                selfieImageUrl: '{{kycSelfieUrl}}'
              })
            }
          }
        },
        {
          name: '7. Delete KYC Asset',
          request: {
            method: 'DELETE',
            header: authHeader,
            url: {
              raw: '{{baseUrl}}/profiles/{{userId}}/kyc?type=selfie',
              host: ['{{baseUrl}}'],
              path: ['profiles', '{{userId}}', 'kyc'],
              query: [{ key: 'type', value: 'selfie' }]
            },
            description: 'Accepted values: document, selfie, all. Deletes the selected KYC file from S3 and clears it from the user profile.'
          }
        },
        {
          name: '8. Register As Owner',
          request: {
            method: 'POST',
            header: authHeader,
            url: { raw: '{{baseUrl}}/profiles/{{userId}}/owner', host: ['{{baseUrl}}'], path: ['profiles', '{{userId}}', 'owner'] }
          }
        },
        {
          name: '9. Get Role History',
          request: {
            method: 'GET',
            header: authHeader,
            url: { raw: '{{baseUrl}}/profiles/{{userId}}/role-history', host: ['{{baseUrl}}'], path: ['profiles', '{{userId}}', 'role-history'] }
          }
        }
      ]
    },
    {
      name: 'Listings',
      item: [
        {
          name: '1. Publish Listing',
          request: {
            method: 'POST',
            header: authedJsonHeader,
            url: { raw: '{{baseUrl}}/listings', host: ['{{baseUrl}}'], path: ['listings'] },
            body: {
              mode: 'raw',
              raw: json({
                ownerId: '{{ownerId}}',
                propertyName: 'Sunrise Apartments',
                placeType: 'APARTMENT',
                roomType: 'PRIVATE_ROOM',
                availableFrom: '2026-04-01',
                furnishingStatus: 'SEMI_FURNISHED',
                totalFloors: 6,
                propertyOnFloor: 'FIRST',
                floorPlan: '2BHK',
                floorType: 'SINGLE',
                balconyAvailable: true,
                ageOfProperty: '0_1_YEARS',
                carParking: 1,
                bikeParking: 2,
                latitude: 12.9716,
                longitude: 77.5946,
                showPreciseLocation: true,
                country: 'India',
                flatHouseDetails: 'Flat 4B',
                streetAddress: '80 Feet Road',
                landmark: 'Near Forum Mall',
                district: 'Koramangala',
                pinCode: '560034',
                city: 'Bangalore',
                bathroomType: 'ATTACHED',
                occupantType: 'ROOMMATES',
                lights: 4,
                fans: 3,
                ac: 1,
                tv: 1,
                beds: 2,
                wardrobes: 2,
                geysers: 1,
                amenities: ['Wifi', 'Washing Machine', 'Stove'],
                customAmenities: ['Water Purifier'],
                description: 'Spacious 2BHK with good ventilation.',
                petsAllowed: false,
                drinkingAllowed: true,
                smokingAllowed: false,
                powerBackup: true,
                photos: [
                  'https://s3.amazonaws.com/flatmate-photos/listings/listing_001/photo_1.jpg',
                  'https://s3.amazonaws.com/flatmate-photos/listings/listing_001/photo_2.jpg',
                  'https://s3.amazonaws.com/flatmate-photos/listings/listing_001/photo_3.jpg',
                  'https://s3.amazonaws.com/flatmate-photos/listings/listing_001/photo_4.jpg',
                  'https://s3.amazonaws.com/flatmate-photos/listings/listing_001/photo_5.jpg'
                ],
                rent: 15000,
                minRent: 14000,
                maxRent: 17000,
                noticePeriod: '2_MONTHS',
                roomsAvailable: 1,
                genderPreference: 'ANY',
                occupancy: 2
              })
            }
          }
        },
        {
          name: '2. Save Draft Listing',
          request: {
            method: 'POST',
            header: authedJsonHeader,
            url: { raw: '{{baseUrl}}/listings/draft', host: ['{{baseUrl}}'], path: ['listings', 'draft'] },
            body: { mode: 'raw', raw: json({ ownerId: '{{ownerId}}', placeType: 'APARTMENT', rent: 15000 }) }
          }
        },
        {
          name: '3. Update Listing',
          request: {
            method: 'PUT',
            header: authedJsonHeader,
            url: { raw: '{{baseUrl}}/listings/{{listingId}}', host: ['{{baseUrl}}'], path: ['listings', '{{listingId}}'] },
            body: { mode: 'raw', raw: json({ propertyName: 'Sunrise Apartments Phase 2', rent: 16000, description: 'Updated listing description' }) }
          }
        },
        {
          name: '4. Get All Listings',
          request: {
            method: 'GET',
            header: authHeader,
            url: { raw: '{{baseUrl}}/listings', host: ['{{baseUrl}}'], path: ['listings'] }
          }
        },
        {
          name: '5. Get Listing By ID',
          request: {
            method: 'GET',
            header: authHeader,
            url: { raw: '{{baseUrl}}/listings/{{listingId}}', host: ['{{baseUrl}}'], path: ['listings', '{{listingId}}'] }
          }
        },
        {
          name: '6. Get Listings By Owner',
          request: {
            method: 'GET',
            header: authHeader,
            url: { raw: '{{baseUrl}}/listings/owner/{{ownerId}}', host: ['{{baseUrl}}'], path: ['listings', 'owner', '{{ownerId}}'] }
          }
        },
        {
          name: '7. Deactivate Listing',
          request: {
            method: 'DELETE',
            header: authHeader,
            url: { raw: '{{baseUrl}}/listings/{{listingId}}', host: ['{{baseUrl}}'], path: ['listings', '{{listingId}}'] }
          }
        },
        {
          name: '8. Get Listing Upload URL',
          request: {
            method: 'GET',
            header: authHeader,
            url: {
              raw: '{{baseUrl}}/listings/{{listingId}}/upload-url?fileName=photo_1.jpg',
              host: ['{{baseUrl}}'],
              path: ['listings', '{{listingId}}', 'upload-url'],
              query: [{ key: 'fileName', value: 'photo_1.jpg' }]
            }
          }
        }
      ]
    },
    {
      name: 'Location',
      item: [
        {
          name: '1. Index Listing Coordinates',
          request: {
            method: 'POST',
            header: authedJsonHeader,
            url: { raw: '{{baseUrl}}/location/index', host: ['{{baseUrl}}'], path: ['location', 'index'] },
            body: { mode: 'raw', raw: json({ listingId: '{{listingId}}', latitude: 12.9716, longitude: 77.5946 }) }
          }
        },
        {
          name: '2. Find Nearby Listings',
          request: {
            method: 'GET',
            header: authHeader,
            url: {
              raw: '{{baseUrl}}/location/nearby?lat=12.9716&lng=77.5946&radius=10&minRent=10000&maxRent=20000&genderPreference=ANY&minRooms=1',
              host: ['{{baseUrl}}'],
              path: ['location', 'nearby'],
              query: [
                { key: 'lat', value: '12.9716' },
                { key: 'lng', value: '77.5946' },
                { key: 'radius', value: '10' },
                { key: 'minRent', value: '10000' },
                { key: 'maxRent', value: '20000' },
                { key: 'genderPreference', value: 'ANY' },
                { key: 'minRooms', value: '1' }
              ]
            }
          }
        }
      ]
    },
    {
      name: 'Swipes & Matches',
      item: [
        {
          name: '1. Swipe On Listing',
          request: {
            method: 'POST',
            header: authedJsonHeader,
            url: { raw: '{{baseUrl}}/swipes', host: ['{{baseUrl}}'], path: ['swipes'] },
            body: { mode: 'raw', raw: json({ seekerId: '{{seekerId}}', listingId: '{{listingId}}', type: 'RIGHT' }) }
          }
        },
        {
          name: '2. Get Listing Interests',
          request: {
            method: 'GET',
            header: authHeader,
            url: { raw: '{{baseUrl}}/swipes/listing/{{listingId}}/interests', host: ['{{baseUrl}}'], path: ['swipes', 'listing', '{{listingId}}', 'interests'] }
          }
        },
        {
          name: '3. Approve Match',
          request: {
            method: 'POST',
            header: authedJsonHeader,
            url: { raw: '{{baseUrl}}/matches/approve', host: ['{{baseUrl}}'], path: ['matches', 'approve'] },
            body: { mode: 'raw', raw: json({ seekerId: '{{seekerId}}', ownerId: '{{ownerId}}' }) }
          }
        },
        {
          name: '4. Get Matches',
          request: {
            method: 'GET',
            header: authHeader,
            url: { raw: '{{baseUrl}}/matches/{{userId}}', host: ['{{baseUrl}}'], path: ['matches', '{{userId}}'] }
          }
        }
      ]
    },
    {
      name: 'Chat',
      item: [
        {
          name: '1. Mark Chat As Read',
          request: {
            method: 'POST',
            header: authedJsonHeader,
            url: { raw: '{{baseUrl}}/chat/read', host: ['{{baseUrl}}'], path: ['chat', 'read'] },
            body: { mode: 'raw', raw: json({ seekerId: '{{seekerId}}', ownerId: '{{ownerId}}', role: 'SEEKER' }) }
          }
        },
        {
          name: '2. Get Chat Upload URL',
          request: {
            method: 'GET',
            header: authHeader,
            url: {
              raw: '{{baseUrl}}/chat/upload-url?fileName=voice-note.m4a',
              host: ['{{baseUrl}}'],
              path: ['chat', 'upload-url'],
              query: [{ key: 'fileName', value: 'voice-note.m4a' }]
            }
          }
        },
        {
          name: '3. Get Chat History',
          request: {
            method: 'GET',
            header: authHeader,
            url: { raw: '{{baseUrl}}/chat/{{matchId}}/history', host: ['{{baseUrl}}'], path: ['chat', '{{matchId}}', 'history'] }
          }
        }
      ]
    },
    {
      name: 'Notifications',
      item: [
        {
          name: '1. Save Device Token',
          request: {
            method: 'POST',
            header: authedJsonHeader,
            url: { raw: '{{baseUrl}}/notifications/token', host: ['{{baseUrl}}'], path: ['notifications', 'token'] },
            body: { mode: 'raw', raw: json({ userId: '{{userId}}', token: '<FCM_DEVICE_TOKEN>' }) }
          }
        },
        {
          name: '2. Send Notification',
          request: {
            method: 'POST',
            header: authedJsonHeader,
            url: { raw: '{{baseUrl}}/notifications/send', host: ['{{baseUrl}}'], path: ['notifications', 'send'] },
            body: { mode: 'raw', raw: json({ userId: '{{userId}}', title: 'New Interest', body: 'Someone liked your listing.' }) }
          }
        },
        {
          name: '3. Get Inbox',
          request: {
            method: 'GET',
            header: authHeader,
            url: { raw: '{{baseUrl}}/notifications/inbox/{{userId}}', host: ['{{baseUrl}}'], path: ['notifications', 'inbox', '{{userId}}'] }
          }
        }
      ]
    }
  ]
};

fs.writeFileSync('flatmate-postman-collection.json', JSON.stringify(collection, null, 2));
console.log('Postman collection generated successfully');
