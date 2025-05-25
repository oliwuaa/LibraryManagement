import React from 'react';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import WelcomePage from './pages/WelcomePage.jsx';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import UserProfilePage from './pages/ProfilePage';
import BookPage from './pages/BookPage';
import LoanPage from './pages/LoanPage';
import ReservationPage from './pages/ReservationPage';
import MyLibraryPage from './pages/MyLibraryPage';
import BookDetailPage from './pages/BookDetailPage';
import ManageResourcesPage from './pages/ManageResources';
import ManageUsersPage from './pages/ManageUsersPage';
import ManageLibrariansPage from './pages/ManageLibrariansPage';
import HomeLibPage from './pages/HomeLibPage';
import HomeAdminPage from './pages/HomeAdminPage';
import HomeUserPage from './pages/HomeUserPage';
import ProtectedRoute from './components/ProtectedRoute';


const App = () => {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<WelcomePage/>}/>
                <Route path="/login" element={<LoginPage/>}/>
                <Route path="/register" element={<RegisterPage/>}/>

                <Route
                    path="/profile"
                    element={
                        <ProtectedRoute>
                            <UserProfilePage/>
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/reservations"
                    element={
                        <ProtectedRoute>
                            <ReservationPage/>
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/loans"
                    element={
                        <ProtectedRoute>
                            <LoanPage/>
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/library"
                    element={
                        <ProtectedRoute>
                            <MyLibraryPage/>
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/home-l"
                    element={
                        <ProtectedRoute>
                            <HomeLibPage/>
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/home-a"
                    element={
                        <ProtectedRoute>
                            <HomeAdminPage/>
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/home-u"
                    element={
                        <ProtectedRoute>
                            <HomeUserPage/>
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/books"
                    element={
                        <ProtectedRoute>
                            <BookPage/>
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/users"
                    element={
                        <ProtectedRoute>
                            <ManageUsersPage/>
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/users/library/:libraryId"
                    element={
                        <ProtectedRoute>
                            <ManageLibrariansPage/>
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/manage-resources"
                    element={
                        <ProtectedRoute>
                            <ManageResourcesPage/>
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/manage-resources/:libraryId"
                    element={
                        <ProtectedRoute>
                            <ManageResourcesPage/>
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/book/:bookId"
                    element={
                        <ProtectedRoute>
                            <BookDetailPage/>
                        </ProtectedRoute>
                    }
                />
            </Routes>
        </Router>
    );
};

export default App;
