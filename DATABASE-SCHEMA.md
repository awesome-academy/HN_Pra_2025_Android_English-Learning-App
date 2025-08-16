# English Learning App - Database Schema

## Overview
This document describes the simplified database schema for the English Learning App. The system focuses on core entities: Users, Lessons, Words, and UserLessonProgress for tracking user learning progress.

## 🏗️ Database Architecture

### Core Entity Relationships
```
Lessons (1) ──→ Words (Many)
    ↓
Users (Many) ←──→ Lessons (Many) [UserLessonProgress]

Key Design Principles:
- Users learn lessons directly (simplified - no course structure)
- Each lesson contains words based on difficulty level
- Progress tracking occurs at the lesson level
- Lesson difficulty determines word count: Easy < Medium < Advanced
- Suggested lessons = lessons user hasn't started yet
```

## 📋 Firestore Collections

### 1. Core Content Tables


#### `lessons`
```typescript
{
  id: string                    // Primary key
  courseId: string              // Optional grouping field (legacy)
  title: string                 // Lesson title
  lessonNumber: number          // Order/sequence number
  description: string           // Lesson description
  duration: string              // "10 min"
  difficulty: "EASY" | "MEDIUM" | "ADVANCED"  // Lesson difficulty level
  totalPoints: number           // Maximum points
  wordIds: string[]             // Foreign keys → words
  exercises: string[]           // Exercise types
  videoUrl: string              // Video content
  audioUrl: string              // Audio content
  imageUrl: string              // Lesson image
  isActive: boolean            // Active status
  createdAt: Date              // Creation timestamp
  // Note: isStarted is computed at runtime, not stored in database
}
```

**Suggested Lessons Logic:**
- Lessons are displayed as "suggested" when user hasn't started them yet
- The `isStarted` field is computed by checking if user has any `UserLessonProgress` record for that lesson
- No separate `SuggestedCourse` table needed - suggestion status is determined dynamically
- Use `LessonRepository.getSuggestedLessons(userId)` to get lessons user hasn't started

#### `words`
```typescript
{
  id: string                    // Primary key
  word: string                  // Word text
  definition: string            // Word meaning
  pronunciation: string         // Pronunciation guide
  phonetic: string              // IPA notation
  partOfSpeech: string          // "noun", "verb", etc.
  example: string               // Usage example
  soundUrl: string              // Pronunciation audio
  imageUrl: string              // Word illustration
  lessonId: string              // Foreign key → lessons
  difficulty: string            // "easy", "medium", "advanced"
}
```

### 2. User Management

#### `users`
```typescript
{
  id: string                    // Primary key (Firebase Auth UID)
  email: string                 // User email
  displayName: string           // Full name
  photoURL: string              // Profile picture
  provider: string              // "google", "email", "facebook"
  isEmailVerified: boolean      // Email verification status
  currentLevel: string          // "Beginner", "Intermediate", "Advanced"
  totalPoints: number           // Lifetime points earned
  streak: number                // Current study streak
  lessonsCompleted: number      // Total lessons completed
  wordsLearned: number          // Total words learned
  joinedAt: Date                // Registration date
  lastActiveAt: Date            // Last app usage
  preferences: {                // User settings
    language: string            // UI language
    notifications: boolean      // Push notification setting
    studyReminder: string       // Daily reminder time
  }
}
```

### 3. Progress Tracking (Many-to-Many Relationship)

#### `userLessonProgress`
**Purpose:** Detailed tracking of individual lesson progress and attempts (Many-to-Many: Users ↔ Lessons)
```typescript
{
  id: string                    // Primary key
  userId: string                // Foreign key → users
  lessonId: string              // Foreign key → lessons
  isStarted: boolean            // Has user started lesson
  isCompleted: boolean          // Lesson completion status
  currentPoints: number         // Points earned
  totalPoints: number           // Maximum points (100)
  progressPercentage: number    // Progress (0-100)
  timeSpentMinutes: number      // Time spent on lesson
  attempts: number              // Number of attempts
  bestScore: number             // Best score achieved
  wordsLearned: number          // Words learned in this lesson
  totalWords: number            // Total words in lesson
  completedExercises: string[]  // Finished exercise types
  learnedWordIds: string[]      // IDs of words learned
  startedAt: Date               // First access time
  completedAt: Date | null      // Completion timestamp
  lastAccessedAt: Date          // Last access time
}
```


## 🔄 Data Flow Examples

### 1. User Starts a Lesson
```javascript
// Create UserLessonProgress record
const progressId = `${userId}_${lessonId}`;
await db.collection('userLessonProgress').doc(progressId).set({
  id: progressId,
  userId: "user_001",
  lessonId: "lesson_001",
  isStarted: true,
  isCompleted: false,
  currentPoints: 0,
  totalPoints: 100,
  progressPercentage: 0,
  wordsLearned: 0,
  totalWords: 15, // Based on lesson difficulty
  startedAt: new Date(),
  lastAccessedAt: new Date()
});
```

### 2. User Learns Words in Lesson
```javascript
// Update words learned progress
await db.collection('userLessonProgress').doc(progressId).update({
  learnedWordIds: admin.firestore.FieldValue.arrayUnion("word_001", "word_002"),
  wordsLearned: admin.firestore.FieldValue.increment(2),
  progressPercentage: Math.floor((wordsLearned / totalWords) * 100),
  lastAccessedAt: new Date()
});
```

### 3. User Completes Lesson
```javascript
// 1. Mark lesson as completed
await db.collection('userLessonProgress').doc(progressId).update({
  isCompleted: true,
  currentPoints: 85,
  bestScore: 85,
  progressPercentage: 100,
  completedAt: new Date(),
  lastAccessedAt: new Date()
});

// 2. Update user's overall stats
await db.collection('users').doc('user_001').update({
  totalPoints: admin.firestore.FieldValue.increment(85),
  lessonsCompleted: admin.firestore.FieldValue.increment(1),
  wordsLearned: admin.firestore.FieldValue.increment(15),
  lastActiveAt: new Date()
});
```

### 4. Query User's Progress
```javascript
// Get user's lesson progress
const userProgress = await db.collection('userLessonProgress')
  .where('userId', '==', 'user_001')
  .orderBy('lastAccessedAt', 'desc')
  .get();

// Get completed lessons
const completedLessons = await db.collection('userLessonProgress')
  .where('userId', '==', 'user_001')
  .where('isCompleted', '==', true)
  .get();

// Get lessons in progress
const inProgressLessons = await db.collection('userLessonProgress')
  .where('userId', '==', 'user_001')
  .where('isStarted', '==', true)
  .where('isCompleted', '==', false)
  .get();
```

## 📊 Analytics Queries

### User Dashboard Statistics
```javascript
// Get user's overall stats
const user = await db.collection('users').doc(userId).get();
const userStats = {
  totalPoints: user.data().totalPoints,
  lessonsCompleted: user.data().lessonsCompleted,
  wordsLearned: user.data().wordsLearned,
  streak: user.data().streak
};

// Recent lesson activity
const recentLessons = await db.collection('userLessonProgress')
  .where('userId', '==', userId)
  .orderBy('lastAccessedAt', 'desc')
  .limit(5)
  .get();

// Progress by difficulty level
const progressByDifficulty = await Promise.all([
  // Get easy lessons progress
  db.collection('userLessonProgress')
    .where('userId', '==', userId)
    .get()
    .then(snapshot => {
      // Filter by lesson difficulty in app logic
      return snapshot.docs.filter(doc => {
        // Would need to join with lessons collection
      });
    })
]);
```

### Admin Analytics
```javascript
// Most popular lessons
const popularLessons = await db.collection('userLessonProgress')
  .where('isStarted', '==', true)
  .get()
  .then(snapshot => {
    // Group by lessonId and count
    const lessonCounts = {};
    snapshot.docs.forEach(doc => {
      const lessonId = doc.data().lessonId;
      lessonCounts[lessonId] = (lessonCounts[lessonId] || 0) + 1;
    });
    return lessonCounts;
  });

// Completion rates by lesson
const completionRates = await db.collection('userLessonProgress')
  .get()
  .then(snapshot => {
    const stats = {};
    snapshot.docs.forEach(doc => {
      const data = doc.data();
      const lessonId = data.lessonId;
      if (!stats[lessonId]) {
        stats[lessonId] = { started: 0, completed: 0 };
      }
      stats[lessonId].started++;
      if (data.isCompleted) {
        stats[lessonId].completed++;
      }
    });
    return stats;
  });
```

## 🚀 Import Script Usage

Run the complete schema import:
```bash
node firebase-schema-complete.js
```

This creates all collections with sample data including:
- 15 lessons (5 Easy, 5 Medium, 5 Advanced) with different word counts
- 50+ words distributed across lessons based on difficulty
- 2 sample users with realistic lesson progress data
- UserLessonProgress records showing various completion states
- Examples of suggested vs started lessons for each user

## 🔐 Security Considerations

### Firestore Security Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own user document
    match /users/{userId} {
      allow read, write: if request.auth != null && 
        request.auth.uid == userId;
    }
    
    // Users can only read/write their own progress data
    match /userLessonProgress/{document} {
      allow read, write: if request.auth != null && 
        request.auth.uid == resource.data.userId;
    }
    
    // Core content is readable by all authenticated users
    match /lessons/{document} {
      allow read: if request.auth != null;
    }
    
    match /words/{document} {
      allow read: if request.auth != null;
    }
  }
}
```

## 🎯 Key Benefits of This Design

### Simplified Architecture
- **Reduced Complexity**: No category/course enrollment - users learn lessons directly
- **Clear Relationships**: Linear progression from courses → lessons → words
- **Single Progress Entity**: UserLessonProgress handles all tracking needs

### Scalability
- **Difficulty-Based Content**: Easy lessons have fewer words, advanced have more
- **Flexible Progress Tracking**: Track words learned, time spent, attempts, scores
- **Efficient Queries**: Simple structure enables fast Firebase queries

### Learning Experience
- **Granular Progress**: Track individual word learning within lessons
- **Adaptive Difficulty**: Lessons adjust word count based on difficulty level
- **Comprehensive Stats**: User-level aggregated statistics for motivation

This streamlined schema focuses on the core learning experience while maintaining rich progress tracking capabilities.