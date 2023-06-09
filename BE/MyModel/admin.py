from django.contrib import admin

from .models import User, Post, Comment, SubComment
 
# Register your models here.
admin.site.register([User, Post, Comment, SubComment])
# Register your models here.
