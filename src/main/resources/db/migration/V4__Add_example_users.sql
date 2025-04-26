DELETE FROM users;

INSERT INTO users (password, email, name, surname, role, library_id)
VALUES
    ('$2a$10$EV8y5MiU0pqv9u2LjKn88uRtvLqNgLRW8W9NPjDo9lMwn0HaCJom2', 'john.doe@example.com', 'John', 'Doe', 'USER', NULL),
    ('$2a$10$KEDyrOexZJTV.4YvjJ/64egFVK1xmfRCa0AcB4Um1jR2wvbd9sT.C', 'jane.smith@example.com', 'Jane', 'Smith', 'LIBRARIAN', 1),
    ('$2a$10$2T6sjp6A/DR/bI6sGmfPguTKQcuDC9LxnGHsPAJdr3pRrC8FDKyTC', 'user2@example.com', 'Emily', 'Jones', 'USER', NULL),
    ('$2a$10$tCX50FXdS/TxYgHmwZ4Y4eCFxF02OAk78vLbMhzD5A/fmE0VZ2ca2', 'michael.brown@example.com', 'Michael', 'Brown', 'USER', NULL),
    ('$2a$10$6g9fv97qBRG6g2gTkUKTneAE8r6SVh0AAVrYhzPu9LhP3roXy2aCe', 'susan.white@example.com', 'Susan', 'White', 'LIBRARIAN', 2),
    ('$2a$10$LYXxZ/RAc/YDnX1RXOmEmOB8MeW8y/FV1By.N7bS1WmsxwzvRVpgW', 'david.taylor@example.com', 'David', 'Taylor', 'USER', NULL),
    ('$2a$10$uB/5L1DK1.X1iCPoeEZV8ukqKnMSSOH4UdeEt4v62RMEp8tMwRv2S', 'lisa.moore@example.com', 'Lisa', 'Moore', 'LIBRARIAN', 1),
    ('$2a$10$byGyBHzRC.vhCTJK4Zib8e/Wk5rhHZ84ulTAh8ExVaPFlu9wcpA3K', 'robert.jackson@example.com', 'Robert', 'Jackson', 'USER', NULL),
    ('$2a$10$lGFvW9uOgNa86n7aR90czuNDHKHQKBBjk2fqaTn7Yj.5D7hvNjScu', 'nancy.harris@example.com', 'Nancy', 'Harris', 'LIBRARIAN', 3),
    ('$2a$10$FbUvAA5HGUKQ9rc6YbB7A.wkSBu/dz1YNB1BWaAYHdEEy3jT1bnIa', 'charles.clark@example.com', 'Charles', 'Clark', 'USER', NULL),
    ('$2a$10$Ekuz/XXpEJmmjO4HHm6gvOGVmCZb2ktWsLFKqaC5OBybqjN7eW9TK', 'karen.lewis@example.com', 'Karen', 'Lewis', 'USER', NULL),
    ('$2a$10$RAzQQeXz2OcfmQ0Pdr/XMuTLtRffS5AeS2lVi7w1P7zvqklM3bcDq', 'daniel.walker@example.com', 'Daniel', 'Walker', 'LIBRARIAN', 2),
    ('$2a$10$6xCdMFcdZyXYPyZ8.AA2NOKRBAdnM6L2kuMvzU/b0YbJz7H3ujYXu', 'betty.hall@example.com', 'Betty', 'Hall', 'USER', NULL),
    ('$2a$10$By6HqnybEuYQ0g16ew/52et9rfptFhB/qHiqz0onwx0AnZqBDLw/W', 'thomas.allen@example.com', 'Thomas', 'Allen', 'LIBRARIAN', 3),
    ('$2a$10$sPuB1MWbHY.F4yrZJkANeu8qzMpSufN2XUTtkpl0kAdh7Ydl4QmQy', 'sarah.young@example.com', 'Sarah', 'Young', 'USER', NULL),
    ('$2a$10$NuKEF.0XZ2rkRKCe.Wk3zOCZJuNz5nMZYloNu7dp5RzsoEnZ/FGB2', 'mark.king@example.com', 'Mark', 'King', 'USER', NULL),
    ('$2a$10$gtcM4vW3hyvqNK5hE89FeuqyyYPmfzRyS13g1k8h0THtsnkgRzG/y', 'judy.wright@example.com', 'Judy', 'Wright', 'LIBRARIAN', 1),
    ('$2a$10$PC3z/3VnZTXzRYMsG6eycuQQk5mU3mMaY/JQ/r96bAjfEIoExkXa2', 'kevin.scott@example.com', 'Kevin', 'Scott', 'USER', NULL),
    ('$2a$10$Q8RTyx6XsW0AAHQ6v3m2mO/2DJn8fZG00gkp45oQScuOEe1uD7FBq', 'barbara.green@example.com', 'Barbara', 'Green', 'LIBRARIAN', 2),
    ('$2a$10$LBpbORWEhRdrNkPCsGQ7ZetQ8tWz1HgCCmQsCDnK1l6OEzMF0nZtG', 'steven.adams@example.com', 'Steven', 'Adams', 'USER', NULL),

    ('$2y$10$P8NU.f0uwRM1Dw1fcpxdzurn8Z09G3Yw5f0HGoLfZxhdj7XVA8TO6', 'admin@example.com', 'Admin', 'Example', 'ADMIN', NULL),      -- admin
    ('$2y$10$pVupQkXLAQa9XTnPV1YEdO.LUdV7bhd9WBu/T7i8.H7oWD5jV55v2', 'user@example.com', 'User', 'Example', 'USER', NULL),          -- user
    ('$2y$10$N8teIiP1rtFy/hfJSsIehOF4RM3n3xTOYDM3nNvwJMpkPBWU.qdV2', 'librarian@example.com', 'Libby', 'Example', 'LIBRARIAN', 1); -- librarian