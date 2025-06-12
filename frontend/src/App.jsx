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

const App = () => {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<WelcomePage/>}/>
                <Route path="/login" element={<LoginPage/>}/>
                <Route path="/register" element={<RegisterPage/>}/>
                <Route path="/profile" element={<UserProfilePage/>}/>
                <Route path="/reservations" element={<ReservationPage/>}/>
                <Route path="/loans" element={<LoanPage/>}/>
                <Route path="/library" element={<MyLibraryPage/>}/>
                <Route path="/home-l" element={<HomeLibPage/>}/>
                <Route path="/home-a" element={<HomeAdminPage/>}/>
                <Route path="/home-u" element={<HomeUserPage/>}/>
                <Route path="/books" element={<BookPage/>}/>
                <Route path="/users" element={<ManageUsersPage/>}/>
                <Route path="/users/library/:libraryId" element={<ManageLibrariansPage/>}/>
                <Route path="/manage-resources" element={<ManageResourcesPage/>}/>
                <Route path="/manage-resources/:libraryId" element={<ManageResourcesPage/>}/>
                <Route path="/book/:bookId" element={<BookDetailPage/>}/>
            </Routes>
        </Router>
    );
};

export default App;
