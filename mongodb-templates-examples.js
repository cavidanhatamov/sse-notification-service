// Use the correct database
use('notifications-db');

db.templates.insertOne({
    "_id": "payment-success-sms",
    "name": "Payment Success SMS",
    "channel": "SMS",
    "active": true,
    "params": [
        {
            "key": "amount",
            "type": "string",
            "required": true,
            "description": "Payment amount"
        },
        {
            "key": "transactionId",
            "type": "string",
            "required": true,
            "description": "Transaction ID"
        }
    ],
    "translations": {
        "en": {
            "subject": "Payment Successful",
            "content": "Payment of ${amount} was successful. Transaction ID: ${transactionId}"
        },
        "az": {
            "subject": "Ödəniş Uğurlu",
            "content": "${amount} məbləğində ödəniş uğurla həyata keçirildi. Tranzaksiya ID: ${transactionId}"
        },
        "ru": {
            "subject": "Платёж успешен",
            "content": "Платёж на сумму ${amount} был успешно выполнен. ID транзакции: ${transactionId}"
        }
    },
    "meta": {
        "createdBy": "system",
        "createdAt": new Date(),
        "updatedAt": new Date()
    }
});