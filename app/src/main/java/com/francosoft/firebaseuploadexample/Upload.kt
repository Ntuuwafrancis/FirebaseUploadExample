package com.francosoft.firebaseuploadexample

import com.google.firebase.database.Exclude

data class Upload(var name: String? = null,
                  var imageUrl: String? = null,
                  @Exclude var key: String? = null
            )

