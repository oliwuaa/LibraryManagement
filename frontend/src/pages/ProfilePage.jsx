import React, {useState, useEffect} from 'react';
import '../styles/ProfilePage.css';
import '../App.css';
import Navbar from '../components/Navbar.jsx';
import LibraryDetails from '../components/LibraryInfo.jsx';
import {fetchWithAuth} from '../Api.js';

const UserProfilePage = () => {
    const [userInfo, setUserInfo] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchUserInfo = async () => {
            try {
                const response = await fetchWithAuth('/users/me');
                if (response?.ok) {
                    const data = await response.json();
                    setUserInfo(data);
                } else {
                    throw new Error('Error fetching user data');
                }
            } catch (err) {
                setError(err.message);
            }
        };

        fetchUserInfo();
    }, []);

    useEffect(() => {
        const adjustParagraphWidth = () => {
            const paragraphs = document.querySelectorAll('.user-info-container p');
            let maxWidth = 0;

            paragraphs.forEach(p => {
                p.style.width = 'auto';
                maxWidth = Math.max(maxWidth, p.offsetWidth);
            });

            paragraphs.forEach(p => {
                p.style.width = `${maxWidth}px`;
            });
        };

        if (userInfo) {
            adjustParagraphWidth();
        }
    }, [userInfo]);

    if (error) {
        return <p>{error}</p>;
    }

    if (!userInfo) {
        return <p>Loading user data...</p>;
    }

    return (
        <div>
            <Navbar/>
            <div className="profile-layout">
                <div className="user-info-container">
                    <h2>Your Profile</h2>
                    <p><strong>Email:</strong> {userInfo.email}</p>
                    {userInfo.name && <p><strong>Name:</strong> {userInfo.name}</p>}
                    {userInfo.surname && <p><strong>Surname:</strong> {userInfo.surname}</p>}
                    <p><strong>Role:</strong> {userInfo.role}</p>
                </div>

                {userInfo.role === 'LIBRARIAN' && userInfo.libraryId && (
                    <div>
                        <LibraryDetails libraryId={userInfo.libraryId}/>
                    </div>
                )}
            </div>
        </div>
    );
};

export default UserProfilePage;